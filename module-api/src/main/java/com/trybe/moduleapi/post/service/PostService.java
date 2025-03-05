package com.trybe.moduleapi.post.service;

import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.exception.participation.NotFoundChallengeParticipationException;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.PostRequest;
import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.post.exception.ForbiddenPostException;
import com.trybe.moduleapi.post.exception.NotFoundPostException;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.entity.PostChallenge;
import com.trybe.modulecore.post.repository.PostChallengeRepository;
import com.trybe.modulecore.post.repository.PostRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final ChallengeRepository challengeRepository;
    private final PostChallengeRepository postChallengeRepository;
    private final ChallengeParticipationRepository participationRepository;


    public PostService(PostRepository postRepository, ChallengeRepository challengeRepository, PostChallengeRepository postChallengeRepository, ChallengeParticipationRepository participationRepository) {
        this.postRepository = postRepository;
        this.challengeRepository = challengeRepository;
        this.postChallengeRepository = postChallengeRepository;
        this.participationRepository = participationRepository;
    }

    @Transactional
    public PostResponse.Detail save(User user, PostRequest.Create request){
        Post post = request.toEntity(user);
        Post savePost = postRepository.save(post);

        if (request.challengeId().isEmpty()) {
            return PostResponse.Detail.from(savePost, List.of());
        }

        for (Long challengeId: request.challengeId()) {
            if (!participationRepository.existsByStatusAndUserIdAndChallengeId(ParticipationStatus.ACCEPTED, user.getId(), challengeId)){
                throw new NotFoundChallengeParticipationException("참여하지 않는 챌린지는 언급할 수 없습니다.");
            }
        }

        List<ChallengeResponse.Summary> summaryChallenges = savePostChallenge(post, request.challengeId());
        return PostResponse.Detail.from(savePost ,summaryChallenges);
    }

    @Transactional(readOnly = true)
    public PostResponse.Detail find(Long id){
        Post post = getPostById(id);
        List<ChallengeResponse.Summary> summaryChallenges = getSummaryChallengesByPostId(post.getId());

        return PostResponse.Detail.from(post, summaryChallenges);
    }

    // 전체 조회 + 필터링(키워드, 카테고리) 조회
    @Transactional(readOnly = true)
    public PageResponse<PostResponse.Summary> findAll(PostRequest.Read request, Pageable pageable){
        Page<Post> posts = postRepository.findAllByKeywordAndCategories(request.keyword(), request.categories(), request.order(), pageable);
        Page<PostResponse.Summary> summaryPostPageDate = posts.map(PostResponse.Summary::from);
        return new PageResponse<>(summaryPostPageDate);
    }

    @Transactional
    public PostResponse.Detail updatePost(User user, Long id, PostRequest.Update request) {
        Post post = getPostById(id);

        checkLoginUserAndPostUser(user, post);

        post.updatePost(request.title(), request.content(), request.category());

        if (request.challengeId().isEmpty()) {
            List<ChallengeResponse.Summary> summaryChallenges = getSummaryChallengesByPostId(post.getId());
            return PostResponse.Detail.from(post, summaryChallenges);
        }

        postChallengeRepository.deleteAllById(request.challengeId());
        List<ChallengeResponse.Summary> summaryChallenges = savePostChallenge(post, request.challengeId());
        return PostResponse.Detail.from(post, summaryChallenges);
    }

    @Transactional
    public void delete(User user, Long id){
        Post post = getPostById(id);

        checkLoginUserAndPostUser(user, post);

        postRepository.deleteById(id);
        postChallengeRepository.deleteAllByPostId(post.getId());
    }

    private static void checkLoginUserAndPostUser(User user, Post post) {
        if (post.getUser().getId() != user.getId()) {
            throw new ForbiddenPostException();
        }
    }

    private Post getPostById(Long id){
        Post post = postRepository.findById(id).orElseThrow(() -> new NotFoundPostException());
        return post;
    }

    private List<ChallengeResponse.Summary> getSummaryChallengesByPostId(Long postId) {
        List<PostChallenge> postChallenges = postChallengeRepository.findAllByPostId(postId);
        return postChallenges.stream()
                             .map(PostChallenge::getChallenge)
                             .map(ChallengeResponse.Summary::from)
                             .collect(Collectors.toList());
    }

    private List<ChallengeResponse.Summary> savePostChallenge(Post post, Set<Long> challengeId){
        List<Challenge> challenges = challengeRepository.findAllByIdIn(challengeId);
        List<ChallengeResponse.Summary> summaryChallenges = challenges
                .stream()
                .map(ChallengeResponse.Summary::from)
                .collect(Collectors.toList());

        for (Challenge challenge: challenges) {
            PostChallenge postChallenge = PostChallenge.builder()
                                                       .post(post)
                                                       .challenge(challenge)
                                                       .build();
            postChallengeRepository.save(postChallenge);
        }
        return summaryChallenges;
    }

}
