package com.trybe.moduleapi.post.service;

import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.post.exception.NotFoundPostException;
import com.trybe.moduleapi.post.fixtures.PostFixtures;
import com.trybe.moduleapi.post.fixtures.PostLikeFixtures;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.post.entity.Post;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {
    @Mock
    private RedisTemplate<String, Long> redisTemplate;
    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostLikeService postLikeService;

    @Test
    @DisplayName("게시글에 좋아요를 누르면 Redis에 저장된다")
    void 게시글에_좋아요를_누르면_Redis에_저장된다() {
        // given
        mockingRedisTemplate();

        User 회원 = UserFixtures.회원;
        String userKey = PostLikeFixtures.createUserKey(회원.getId());

        Long 포스트_ID = PostFixtures.id;
        String postKey = PostLikeFixtures.createPostKey(포스트_ID);
        String deleteKey = PostLikeFixtures.createDeletedKey(포스트_ID);

        Long 좋아요_개수 = PostLikeFixtures.좋아요_개수L;

        when(postRepository.existsById(포스트_ID)).thenReturn(true);
        when(redisTemplate.opsForZSet().score(userKey, 포스트_ID)).thenReturn(null);
        when(redisTemplate.opsForSet().size(postKey)).thenReturn(좋아요_개수);

        // when
        PostResponse.Like 응답 = postLikeService.addLike(회원, PostFixtures.id);

        // then
        assertEquals(응답.likeCount(), 좋아요_개수+1);
        assertEquals(응답.isLiked(), true);
        verify(redisTemplate.opsForSet()).remove(eq(deleteKey), eq(회원.getId()));
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
        mockingRedisTemplate();

        User 회원 = UserFixtures.회원;
        String userKey = PostLikeFixtures.createUserKey(회원.getId());

        Long 포스트_ID = PostFixtures.id;
        String postKey = PostLikeFixtures.createPostKey(포스트_ID);
        String deleteKey = PostLikeFixtures.createDeletedKey(포스트_ID);

        Long 좋아요_개수 = PostLikeFixtures.좋아요_개수L;

        when(postRepository.existsById(포스트_ID)).thenReturn(true);
        when(redisTemplate.opsForZSet().score(userKey, 포스트_ID)).thenReturn(1.000);
        when(redisTemplate.opsForSet().size(postKey)).thenReturn(좋아요_개수);


        // when
        PostResponse.Like 응답 = postLikeService.removeLike(회원, PostFixtures.id);

        // then
        verify(redisTemplate.opsForZSet()).remove(eq(userKey), eq(PostFixtures.id));
        verify(redisTemplate.opsForSet()).remove(eq(postKey), eq(회원.getId()));
        verify(redisTemplate.opsForSet()).add(eq(deleteKey), eq(회원.getId()));
        assertEquals(응답.likeCount(), 좋아요_개수-1);
        assertEquals(응답.isLiked(), false);
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
        mockingRedisTemplate();

        Long 포스트_ID = PostFixtures.id;
        String postKey = PostLikeFixtures.createPostKey(포스트_ID);
        Set<Long> userIds = PostLikeFixtures.게시글_좋아요_누른_유저목록;
        when(redisTemplate.opsForSet().members(postKey)).thenReturn(userIds);

        // When
        postLikeService.removeLikesByPost(PostFixtures.id);

        // Then
        for (Long userId : userIds) {
            String userKey = PostLikeFixtures.createUserKey(userId);
            verify(redisTemplate.opsForZSet(), times(1)).remove(eq(userKey), eq(PostFixtures.id));

            String deleteKey = PostLikeFixtures.createDeletedKey(포스트_ID);
            verify(redisTemplate.opsForSet()).add(eq(deleteKey), eq(userId));
        }
        verify(redisTemplate, times(1)).delete(postKey);
    }

    @Test
    @DisplayName("특정 게시글의 좋아요 수를 조회하면 좋아요 개수가 반환된다.")
    void 특정_게시글의_좋아요_수를_조회하면_좋아요_개수가_반환된다() {
        // given
        SetOperations<String, Long> setOps = mock(SetOperations.class);
        when(redisTemplate.opsForSet()).thenReturn(setOps);

        Long 포스트_ID = PostFixtures.id;
        String postKey = PostLikeFixtures.createPostKey(포스트_ID);
        when(redisTemplate.opsForSet().size(postKey)).thenReturn(5L);

        // when
        int likeCount = postLikeService.count(PostFixtures.id);

        // then
        assertEquals(likeCount, 5);
    }

    @Test
    @DisplayName("회원이 자신이 좋아요 누른 게시글을 조회하면 좋아요 누른 순으로 반환된다.")
    void 회원이_자신이_좋아요_누른_게시글을_조회하면_좋아요_누른_순으로_반환된다() {
        // given
        ZSetOperations<String, Long> zSetOps = mock(ZSetOperations.class);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

        Pageable pageable = PageRequest.of(0, 10);

        User 회원 = UserFixtures.회원;
        String userKey = PostLikeFixtures.createUserKey(회원.getId());
        Set<Long> postIds = PostFixtures.게시글_아이디_목록;
        List<Post> 게시글_목록 = PostFixtures.ID_존재하는_게시글_목록;

        when(redisTemplate.opsForZSet().reverseRange(userKey, 0, -1)).thenReturn(postIds);
        when(postRepository.findAllByIdIn(postIds)).thenReturn(게시글_목록);

        // When
        PageResponse<PostResponse.Summary> 응답 = postLikeService.getLikePostByUser(회원, pageable);

        // Then
        assertEquals(응답.content().size(), 게시글_목록.size());
    }

    private void mockingRedisTemplate(){
        ZSetOperations<String, Long> zSetOps = mock(ZSetOperations.class);
        SetOperations<String, Long> setOps = mock(SetOperations.class);

        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
    }
}
