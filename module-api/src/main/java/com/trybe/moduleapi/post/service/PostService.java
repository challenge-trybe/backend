package com.trybe.moduleapi.post.service;

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
import com.trybe.modulecore.post.repository.CommentRepository;
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
    private final CommentRepository commentRepository;
    private final PostLikeService postLikeService;

    public PostService(PostRepository postRepository, ChallengeRepository challengeRepository, PostChallengeRepository postChallengeRepository, ChallengeParticipationRepository participationRepository, CommentRepository commentRepository, PostLikeService postLikeService) {
        this.postRepository = postRepository;
        this.challengeRepository = challengeRepository;
        this.postChallengeRepository = postChallengeRepository;
        this.participationRepository = participationRepository;
        this.commentRepository = commentRepository;
        this.postLikeService = postLikeService;
    }

    @Transactional
    public PostResponse.Detail save(User user, PostRequest.Create request){
        Post post = request.toEntity(user);
        Post savePost = postRepository.save(post);

        for (Long challengeId: request.challengeIds()) {
            if (!participationRepository.existsByUserIdAndChallengeIdAndStatus(user.getId(), challengeId, ParticipationStatus.ACCEPTED)){
                throw new NotFoundChallengeParticipationException("참여하지 않는 챌린지는 언급할 수 없습니다.");
            }
        }

        List<Challenge> challenges = getChallenges(request.challengeIds());
        savePostChallenge(post,challenges);
        return PostResponse.Detail.from(savePost, challenges, 0);
    }

    @Transactional(readOnly = true)
    public PostResponse.Detail find(Long id){
        Post post = getPostById(id);
        int likes = postLikeService.getPostLikeCount(post.getId());
        List<Challenge> challenges = getChallengesByPostId(post.getId());
        return PostResponse.Detail.from(post, challenges, likes);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse.Summary> findAll(PostRequest.Read request, Pageable pageable){
        Page<Post> posts = postRepository.findAllByKeywordAndCategories(request.keyword(), request.categories(), request.order(), pageable);
        Page<PostResponse.Summary> postPages = posts.map(PostResponse.Summary::from);
        return new PageResponse<>(postPages);
    }

    @Transactional
    public PostResponse.Detail updatePost(User user, Long id, PostRequest.Update request) {
        Post post = getPostById(id);
        int likes = postLikeService.getPostLikeCount(post.getId());

        checkLoginUserAndPostUser(user, post);

        post.updatePost(request.title(), request.content(), request.category());

        postChallengeRepository.deleteAllByPostId(post.getId());
        List<Challenge> challenges = getChallenges(request.challengeIds());
        savePostChallenge(post,challenges);
        return PostResponse.Detail.from(post, challenges, likes);
    }

    @Transactional
    public void delete(User user, Long id){
        Post post = getPostById(id);

        checkLoginUserAndPostUser(user, post);

        postChallengeRepository.deleteAllByPostId(post.getId());
        postLikeService.removeLikesByPost(post.getId());
        commentRepository.deleteAllByPostId(post.getId());
        postRepository.deleteById(id);
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

    private List<Challenge> getChallengesByPostId(Long postId) {
        return postChallengeRepository.findAllByPostId(postId)
                                      .stream()
                                      .map(PostChallenge::getChallenge)
                                      .collect(Collectors.toList());
    }

    private List<Challenge> getChallenges(Set<Long> challengeIds){
        return challengeRepository.findAllByIdIn(challengeIds);
    }

    private void savePostChallenge(Post post, List<Challenge> challenges) {
        for (Challenge challenge: challenges) {
            PostChallenge postChallenge = PostChallenge.builder()
                                                       .post(post)
                                                       .challenge(challenge)
                                                       .build();
            postChallengeRepository.save(postChallenge);
        }
    }

}
