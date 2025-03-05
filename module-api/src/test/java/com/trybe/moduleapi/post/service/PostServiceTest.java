package com.trybe.moduleapi.post.service;

import com.trybe.moduleapi.challenge.exception.participation.NotFoundChallengeParticipationException;
import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.PostRequest;
import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.post.exception.ForbiddenPostException;
import com.trybe.moduleapi.post.exception.NotFoundPostException;
import com.trybe.moduleapi.post.fixtures.PostChallengeFixtures;
import com.trybe.moduleapi.post.fixtures.PostFixtures;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.repository.PostChallengeRepository;
import com.trybe.modulecore.post.repository.PostRepository;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private PostChallengeRepository postChallengeRepository;
    @Mock
    private ChallengeParticipationRepository participationRepository;

    @InjectMocks
    private PostService postService;
    
    @Test
    @DisplayName("포스트 생성 시 생성된 포스트를 응답해준다.")
    void 포스트_생성_시_생성된_포스트를_응답해준다() {
        /* given */
        PostRequest.Create 게시글_생성_요청 = PostFixtures.게시글_생성;
        User 회원 = UserFixtures.회원;
        Post 게시글 = 게시글_생성_요청.toEntity(회원);

        when(postRepository.save(any(Post.class))).thenReturn(게시글);
        when(challengeRepository.findAllByIdIn(PostFixtures.챌린지_Ids)).thenReturn(ChallengeFixtures.챌린지_목록);
        when(participationRepository.existsByStatusAndUserIdAndChallengeId(
                any(ParticipationStatus.class), any(), any())).thenReturn(true);

        /* when */
        PostResponse.Detail postDetail = postService.save(회원, 게시글_생성_요청);

        /* then */
        assertEquals(postDetail.title(), PostFixtures.제목);
        assertEquals(postDetail.content(), PostFixtures.내용);
        assertEquals(postDetail.user().userId(), UserFixtures.회원_아이디);
        assertEquals(postDetail.user().nickname(), UserFixtures.회원_닉네임);
        assertEquals(postDetail.challengeSummary().size(), 3);
    }

    @Test
    @DisplayName("포스트 생성 시 참여하지 않은 챌린지 ID를 보내면 예외를 터뜨린다.")
    void 포스트_생성_시_참여하지_않은_챌린지_ID를_보내면_예외를_터뜨린다() {
        /* given */
        PostRequest.Create 게시글_생성_요청 = PostFixtures.게시글_생성;
        User 회원 = UserFixtures.회원;
        Post 게시글 = 게시글_생성_요청.toEntity(회원);

        when(postRepository.save(any(Post.class))).thenReturn(게시글);
        when(participationRepository.existsByStatusAndUserIdAndChallengeId(
                any(ParticipationStatus.class), any(), any())).thenReturn(false);

        /* when, then */
        assertThrows(NotFoundChallengeParticipationException.class, () -> {
            postService.save(회원, 게시글_생성_요청);
        }, "참여하지 않는 챌린지는 언급할 수 없습니다.");
    }

    @Test
    @DisplayName("포스트 단건 조회 시 포스트를 응답해준다.")
    void 포스트_단건_조회_시_포스트를_응답해준다() {
        /* given */
        Post 게시글 = PostFixtures.게시글;
        when(postRepository.findById(any())).thenReturn(Optional.of(PostFixtures.게시글));
        when(postChallengeRepository.findAllByPostId(any())).thenReturn(PostChallengeFixtures.포스트_챌린지_목록);

        /* when */
        PostResponse.Detail postDetail = postService.find(1L);

        /* then */
        assertEquals(postDetail.title(), 게시글.getTitle());
        assertEquals(postDetail.content(), 게시글.getContent());
        assertEquals(postDetail.user().userId(), 게시글.getUser().getUserId());
        assertEquals(postDetail.user().nickname(), 게시글.getUser().getNickname());
        assertEquals(postDetail.challengeSummary().size(), 1);
    }

    @Test
    @DisplayName("존재하지 않는 포스트 단건 조회 시 예외를 터뜨린다.")
    void 존재하지_않는_포스트_단건_조회_시_예외를_터뜨린다() {
        /* given */
        when(postRepository.findById(any())).thenReturn(Optional.empty());

        /* when, then */
        assertThrows(NotFoundPostException.class, () -> {
            postService.find(1L);
        }, "존재하지 않는 포스트입니다.");
    }

    @Test
    @DisplayName("포스트 필터링 조회 시 페이징으로 응답해준다")
    void 포스트_필터링_조회_시_페이징으로_응답해준다 () {
        /* given */
        PostRequest.Read 게시글_필터링_조회 = PostFixtures.게시글_필터링_조회;
        Pageable 페이지_요청 = PostFixtures.페이지_요청;
        Page<Post> 페이지_응답 = PostFixtures.페이지_응답;
        when(postRepository.findAllByKeywordAndCategories(PostFixtures.검색_키워드,
                                                          PostFixtures.게시글_카테고리,
                                                          PostFixtures.정렬,
                                                          페이지_요청)).thenReturn(페이지_응답);

        /* when */
        PageResponse<PostResponse.Summary> response = postService.findAll(게시글_필터링_조회, 페이지_요청);

        /* then */
        assertEquals(response.size(), 페이지_요청.getPageSize());
        assertEquals(response.totalElements(), PostFixtures.포스트_페이지_응답.totalElements());
    }

    @Test
    @DisplayName("포스트 수정 시 수정된 포스트를 응답해준다")
    void 포스트_수정_시_수정된_포스트를_응답해준다() {
        /* given */
        PostRequest.Update 게시글_수정_요청 = PostFixtures.게시글_수정;

        when(postRepository.findById(any())).thenReturn(Optional.of(PostFixtures.게시글));
        when(challengeRepository.findAllByIdIn(PostFixtures.수정_챌린지_Ids)).thenReturn(List.of(ChallengeFixtures.챌린지(),ChallengeFixtures.챌린지()));


        /* when */
        PostResponse.Detail postDetail = postService.updatePost(UserFixtures.회원,1L, 게시글_수정_요청);

        /* then */
        assertEquals(postDetail.title(), PostFixtures.수정_제목);
        assertEquals(postDetail.content(), PostFixtures.수정_내용);
        assertEquals(postDetail.user().userId(), UserFixtures.회원_아이디);
        assertEquals(postDetail.user().nickname(), UserFixtures.회원_닉네임);
        assertEquals(postDetail.challengeSummary().size(), 2);
    }
    @Test
    @DisplayName("존재하지 않는 포스트 수정 시 예외를 터뜨린다.")
    void 존재하지_않는_포스트_수정_시_예외를_터뜨린다() {
        /* given */
        PostRequest.Update 게시글_수정_요청 = PostFixtures.게시글_수정;

        when(postRepository.findById(any())).thenReturn(Optional.empty());

        /* when, then */
        assertThrows(NotFoundPostException.class, () -> {
            postService.updatePost(UserFixtures.회원,1L, 게시글_수정_요청);
        }, "존재하지 않는 포스트입니다.");
    }

    @Test
    @DisplayName("접근권한 없는 포스트 수정 시 예외를 터뜨린다.")
    void 접근권한_없는_포스트_수정_시_예외를_터뜨린다() {
        /* given */
        PostRequest.Update 게시글_수정_요청 = PostFixtures.게시글_수정;

        User user = spy(UserFixtures.회원_생성("chacha", "chacha@chacha.com"));
        when(user.getId()).thenReturn(100L);

        Post 게시글 = PostFixtures.createPost(user);

        User newUser = spy(UserFixtures.회원_생성("gunny", "gunny@gunny.com"));
        when(user.getId()).thenReturn(100L);

        when(postRepository.findById(any(Long.class))).thenReturn(Optional.of(게시글));

        /* when, then */
        assertThrows(ForbiddenPostException.class, () -> {
            postService.updatePost(newUser,1L, 게시글_수정_요청);
        }, "해당 게시글에 대한 권한이 없습니다.");
    }

    @Test
    @DisplayName("포스트 삭제 시 포스트를 삭제한다.")
    void 포스트_삭제_시_포스트를_삭제한다() {
        /* given */
        User user = UserFixtures.회원;
        Post post = PostFixtures.게시글;
        when(postRepository.findById(any())).thenReturn(Optional.of(post));
        doNothing().when(postRepository).deleteById(any());
        doNothing().when(postChallengeRepository).deleteAllByPostId(any());


        /* when */
        postService.delete(user, 1L);

        /* then */
        verify(postRepository, times(1)).deleteById(any());
        verify(postChallengeRepository, times(1)).deleteAllByPostId(any());

    }

    @Test
    @DisplayName("접근권한 없는 포스트 삭제 시 예외를 터뜨린다.")
    void 접근권한_없는_포스트_삭제_시_예외를_터뜨린다() {
        /* given */
        User user = spy(UserFixtures.회원_생성("chacha", "chacha@chacha.com"));
        when(user.getId()).thenReturn(100L);

        Post 게시글 = PostFixtures.createPost(user);

        User newUser = spy(UserFixtures.회원_생성("gunny", "gunny@gunny.com"));
        when(user.getId()).thenReturn(100L);

        when(postRepository.findById(any())).thenReturn(Optional.of(게시글));

        /* when, then */
        assertThrows(ForbiddenPostException.class, () -> {
            postService.delete(newUser,1L);
        }, "해당 게시글에 대한 권한이 없습니다.");
    }
}
