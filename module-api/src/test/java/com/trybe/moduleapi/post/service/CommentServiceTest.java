package com.trybe.moduleapi.post.service;

import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.CommentRequest;
import com.trybe.moduleapi.post.dto.CommentResponse;
import com.trybe.moduleapi.post.exception.ForbiddenCommentException;
import com.trybe.moduleapi.post.exception.NotFoundCommentException;
import com.trybe.moduleapi.post.exception.NotFoundPostException;
import com.trybe.moduleapi.post.fixtures.CommentFixtures;
import com.trybe.moduleapi.post.fixtures.PostFixtures;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.post.entity.Comment;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.repository.CommentRepository;
import com.trybe.modulecore.post.repository.PostRepository;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("게시글에 댓글을 성공적으로 작성 시 댓글 정보를 반환한다.")
    void 게시글에_댓글을_성공적으로_작성_시_댓글_정보를_반환한다() {
        /* given */
        Long 게시글_ID = PostFixtures.id;
        Post 게시글 = PostFixtures.게시글;
        User 회원 = UserFixtures.회원;
        CommentRequest.Enroll 댓글_등록 = CommentFixtures.댓글_등록;

        when(postRepository.findById(게시글_ID)).thenReturn(Optional.of(게시글));

        /* when */
        CommentResponse.Summary response = commentService.enroll(회원, 게시글_ID, 댓글_등록);

        /* then */
        assertEquals(response.content(), 댓글_등록.content());
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 댓글을 작성 시 예외를 반환한다.")
    void 존재하지_않는_게시글에_댓글을_작성_시_예외를_반환한다() {
        Long 게시글_ID = PostFixtures.id;
        User 회원 = UserFixtures.회원;
        CommentRequest.Enroll 댓글_등록 = CommentFixtures.댓글_등록;

        when(postRepository.findById(게시글_ID)).thenReturn(Optional.empty());

        /* when, then */
        assertThrows(NotFoundPostException.class, () -> commentService.enroll(회원, 게시글_ID, 댓글_등록), "존재하지 않는 게시글입니다.");
    }

    @Test
    @DisplayName("댓글 수정 시 수정한 댓글 정보를 반환한다.")
    void 댓글_수정_시_수정한_댓글_정보를_반환한다() {
        /* given */
        Long 댓글_ID = CommentFixtures.Id;
        Post 게시글 = PostFixtures.게시글;
        User 회원 = UserFixtures.회원;
        Comment 댓글 = CommentFixtures.댓글_생성(회원, 게시글);

        CommentRequest.Update 댓글_수정 = CommentFixtures.댓글_수정;

        when(commentRepository.findById(댓글_ID)).thenReturn(Optional.of(댓글));

        /* when */
        CommentResponse.Summary response = commentService.update(회원, 댓글_ID, 댓글_수정);

        /* then */
        assertEquals(response.content(), 댓글_수정.content());
    }
    
    @Test
    @DisplayName("존재하지 않는 댓글 수정 시 예외를 반환한다.")
    void 존재하지_않는_댓글_수정_시_예외를_반환한다() {
        /* given */
        Long 댓글_ID = CommentFixtures.Id;
        User 회원 = UserFixtures.회원;

        CommentRequest.Update 댓글_수정 = CommentFixtures.댓글_수정;

        when(commentRepository.findById(댓글_ID)).thenReturn(Optional.empty());

        /* when, then */
        assertThrows(NotFoundCommentException.class, () -> commentService.update(회원, 댓글_ID, 댓글_수정), "존재하지 않는 댓글입니다");
    }

    @Test
    @DisplayName("접근권한이 없는 댓글 수정 시 예외를 반환한다.")
    void 접근권한이_없는_댓글_수정_시_예외를_반환한다 () {
        /* given */
        Long 댓글_ID = CommentFixtures.Id;
        Post 게시글 = PostFixtures.게시글;
        User 회원 = spy(UserFixtures.회원);
        User 댓글_작성자 = spy(UserFixtures.회원);
        Comment 댓글 = CommentFixtures.댓글_생성(댓글_작성자, 게시글);

        CommentRequest.Update 댓글_수정 = CommentFixtures.댓글_수정;

        when(commentRepository.findById(댓글_ID)).thenReturn(Optional.of(댓글));
        when(회원.getId()).thenReturn(UserFixtures.회원_PK);
        when(댓글_작성자.getId()).thenReturn(2L);

        /* when, then */
        assertThrows(ForbiddenCommentException.class, () -> commentService.update(회원, 댓글_ID, 댓글_수정), "본인이 작성한 댓글만 수정할 수 있습니다.");
    }

    @Test
    @DisplayName("댓글 삭제 시 댓글을 삭제한다.")
    void 댓글_삭제_시_댓글을_삭제한다 () {
        /* given */
        Long 댓글_ID = CommentFixtures.Id;
        Post 게시글 = PostFixtures.게시글;
        User 회원 = UserFixtures.회원;
        Comment 댓글 = spy(CommentFixtures.댓글_생성(회원, 게시글));

        when(commentRepository.findById(댓글_ID)).thenReturn(Optional.of(댓글));
        when(댓글.getId()).thenReturn(댓글_ID);

        /* when */
        commentService.delete(회원, 댓글_ID);

        /* then */
        verify(commentRepository, times(1)).deleteById(댓글_ID);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 시 예외를 반환한다.")
    void 존재하지_않는_댓글_삭제_시_예외를_반환한다() {
        /* given */
        Long 댓글_ID = CommentFixtures.Id;
        User 회원 = UserFixtures.회원;

        when(commentRepository.findById(댓글_ID)).thenReturn(Optional.empty());

        /* when, then */
        assertThrows(NotFoundCommentException.class, () -> commentService.delete(회원, 댓글_ID), "존재하지 않는 댓글입니다");
    }

    @Test
    @DisplayName("접근권한이 없는 댓글 삭제 시 예외를 반환한다.")
    void 접근권한이_없는_댓글_삭제_시_예외를_반환한다() {
        /* given */
        Long 댓글_ID = CommentFixtures.Id;
        Post 게시글 = PostFixtures.게시글;
        User 회원 = spy(UserFixtures.회원);
        User 댓글_작성자 = spy(UserFixtures.회원);
        Comment 댓글 = spy(CommentFixtures.댓글_생성(댓글_작성자, 게시글));

        when(commentRepository.findById(댓글_ID)).thenReturn(Optional.of(댓글));
        when(회원.getId()).thenReturn(UserFixtures.회원_PK);
        when(댓글_작성자.getId()).thenReturn(2L);

        /* when, then */
        assertThrows(ForbiddenCommentException.class, () -> commentService.delete(회원, 댓글_ID), "본인이 작성한 댓글만 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("내가 작성한 댓글 목록 조회 시 최신순으로 반환한다.")
    void 내가_작성한_댓글_목록_조회_시_최신순으로_반환한다 () {
        /* given */
        Long 회원_ID = UserFixtures.회원_PK;
        Pageable pageable = CommentFixtures.페이지_요청;
        Page<Comment> 댓글_페이지 = CommentFixtures.댓글_페이지;
        User 회원 = spy(UserFixtures.회원);

        when(commentRepository.findAllByUserIdOrderByCreatedAtDesc(회원_ID, pageable)).thenReturn(댓글_페이지);
        when(회원.getId()).thenReturn(UserFixtures.회원_PK);

        /* when */
        PageResponse<CommentResponse.Detail> response = commentService.findAllByUser(회원, pageable);

        /* then */
        assertEquals(댓글_페이지.getTotalElements(), response.totalElements());
    }

    @Test
    @DisplayName("게시글에 달린 댓글 목록 조회 시 오래된 순으로 반환한다")
    void 게시글에_달린_댓글_목록_조회_시_오래된_순으로_반환한다 () {
        /* given */
        Long 게시글_ID = PostFixtures.id;
        Post 게시글 = spy(PostFixtures.게시글);
        Pageable pageable = CommentFixtures.페이지_요청;
        Page<Comment> 댓글_페이지 = CommentFixtures.댓글_페이지;

        when(게시글.getId()).thenReturn(게시글_ID);
        when(postRepository.findById(게시글_ID)).thenReturn(Optional.of(게시글));
        when(commentRepository.findAllByPostIdOrderByCreatedAtAsc(게시글_ID, pageable)).thenReturn(댓글_페이지);

        /* when */
        PageResponse<CommentResponse.Summary> response = commentService.findAllByPost(게시글_ID, pageable);

        /* then */
        assertEquals(댓글_페이지.getTotalElements(), response.totalElements());
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 대한 댓글 목록 조회 시 예외를 반환한다")
    void 존재하지_않는_게시글에_대한_댓글_목록_조회_시_예외를_반환한다 () {
        /* given */
        Long 게시글_ID = PostFixtures.id;
        Pageable pageable = CommentFixtures.페이지_요청;

        when(postRepository.findById(게시글_ID)).thenReturn(Optional.empty());

        /* when, then */
        assertThrows(NotFoundPostException.class, () -> commentService.findAllByPost(게시글_ID,pageable), "존재하지 않는 게시글입니다.");
    }
}
