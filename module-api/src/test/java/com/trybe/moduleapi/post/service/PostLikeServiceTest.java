package com.trybe.moduleapi.post.service;

import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.post.exception.NotFoundPostException;
import com.trybe.moduleapi.post.fixtures.PostFixtures;
import com.trybe.moduleapi.post.fixtures.PostLikeFixtures;
import com.trybe.moduleapi.post.service.event.pub.PostEventPublisher;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.repository.PostLikeCache;
import com.trybe.modulecore.post.repository.PostRepository;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {
    @Mock
    private PostLikeCache postLikeCache;
    @Mock
    private PostRepository postRepository;
    @Mock
    private PostEventPublisher eventPublisher;

    @InjectMocks
    private PostLikeService postLikeService;

    @Test
    @DisplayName("게시글에 좋아요를 누르면 Redis에 저장된다")
    void 게시글에_좋아요를_누르면_Redis에_저장된다() {
        // given
        User 회원 = UserFixtures.회원;
        Long 포스트_ID = PostFixtures.id;

        int 좋아요_개수 = PostLikeFixtures.좋아요_개수;
        when(postRepository.existsById(포스트_ID)).thenReturn(true);
        when(postLikeCache.getPostLikeCount(포스트_ID)).thenReturn(좋아요_개수);
        when(postLikeCache.alreadyLike(회원.getId(), 포스트_ID)).thenReturn(false);

        // when
        PostResponse.Like 응답 = postLikeService.addLike(회원, PostFixtures.id);

        // then
        assertEquals(응답.likeCount(), 좋아요_개수+1);
        assertEquals(응답.isLiked(), true);

        verify(eventPublisher, times(1)).publish(PostLikeFixtures.좋아요_추가_이벤트(포스트_ID));
        verify(postLikeCache, times(1)).alreadyLike(회원.getId(), 포스트_ID);
        verify(postLikeCache, times(1)).addLike(회원.getId(), 포스트_ID);
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 좋아요를 누르면 예외를 반환한다.")
    void 존재하지_않는_게시글에_좋아요를_누르면_예외를_반환한다() {
        // given
        User 회원 = UserFixtures.회원;
        when(postRepository.existsById(PostFixtures.id)).thenReturn(false);

        // when
        // then
        assertThrows(NotFoundPostException.class, () -> postLikeService.addLike(회원,PostFixtures.id));
    }

    @Test
    @DisplayName("게시글에 대한 좋아요를 삭제하면 Redis에서 삭제된다.")
    void 게시글에_대한_좋아요를_삭제하면_Redis에서_삭제된다() {
        // given
        User 회원 = UserFixtures.회원;
        Long 포스트_ID = PostFixtures.id;

        int 좋아요_개수 = PostLikeFixtures.좋아요_개수;
        when(postRepository.existsById(포스트_ID)).thenReturn(true);
        when(postLikeCache.getPostLikeCount(포스트_ID)).thenReturn(좋아요_개수);
        when(postLikeCache.alreadyLike(회원.getId(), 포스트_ID)).thenReturn(true);

        // when
        PostResponse.Like 응답 = postLikeService.removeLike(회원, PostFixtures.id);

        // then
        assertEquals(응답.likeCount(), 좋아요_개수-1);
        assertEquals(응답.isLiked(), false);

        assertEquals(응답.likeCount(), 좋아요_개수-1);
        assertEquals(응답.isLiked(), false);
      
        verify(postLikeCache, times(1)).alreadyLike(회원.getId(), 포스트_ID);
        verify(postLikeCache, times(1)).removeLike(회원.getId(), 포스트_ID);
        verify(eventPublisher, times(1)).publish(PostLikeFixtures.좋아요_삭제_이벤트(포스트_ID));
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 좋아요를 삭제하면 예외를 반환한다")
    void 존재하지_않는_게시글에_좋아요를_삭제하면_예외를_반환한다() {
        // given
        User 회원 = UserFixtures.회원;
        when(postRepository.existsById(PostFixtures.id)).thenReturn(false);

        // when
        // then
        assertThrows(NotFoundPostException.class, () -> postLikeService.removeLike(회원,PostFixtures.id));
    }


    @Test
    @DisplayName("게시글이 삭제되면 해당 게시글에 대한 모든 좋아요는 삭제된다.")
    void 게시글이_삭제되면_해당_게시글에_대한_모든_좋아요는_삭제된다() {
        // given
        Long 포스트_ID = PostFixtures.id;

        // When
        postLikeService.removeLikesByPost(포스트_ID);

        // Then
        verify(postLikeCache, times(1)).removeLikesByPost(포스트_ID);
    }

    @Test
    @DisplayName("특정 게시글의 좋아요 수를 조회하면 좋아요 개수가 반환된다.")
    void 특정_게시글의_좋아요_수를_조회하면_좋아요_개수가_반환된다() {
        // given
        Long 포스트_ID = PostFixtures.id;
        int 좋아요_개수 = PostLikeFixtures.좋아요_개수;
        when(postLikeCache.getPostLikeCount(포스트_ID)).thenReturn(좋아요_개수);

        // when
        int likeCount = postLikeService.getPostLikeCount(PostFixtures.id);

        // then
        assertEquals(likeCount, 좋아요_개수);
    }

    @Test
    @DisplayName("회원이 자신이 좋아요 누른 게시글을 조회하면 좋아요 누른 순으로 반환된다.")
    void 회원이_자신이_좋아요_누른_게시글을_조회하면_좋아요_누른_순으로_반환된다() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = start + pageable.getPageSize() - 1;

        User 회원 = UserFixtures.회원;
        Set<Long> postIds = PostFixtures.게시글_아이디_목록;
        List<Post> 게시글_목록 = PostFixtures.ID_존재하는_게시글_목록;

        when(postLikeCache.getLikePostIdsByUser(회원.getId(), start, end)).thenReturn(postIds);
        when(postRepository.findAllByIdIn(postIds)).thenReturn(게시글_목록);

        // When
        PageResponse<PostResponse.Summary> 응답 = postLikeService.getLikePostByUser(회원, pageable);

        // Then
        assertEquals(응답.content().size(), 게시글_목록.size());
    }
}
