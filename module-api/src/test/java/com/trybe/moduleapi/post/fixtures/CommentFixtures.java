package com.trybe.moduleapi.post.fixtures;

import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.CommentRequest;
import com.trybe.moduleapi.post.dto.CommentResponse;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.post.entity.Comment;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public class CommentFixtures {
    public static final Long Id = 1L;
    public static final String 내용 = "댓글 테스트 내용입니다.";
    public static final String 수정_내용 = "댓글 테스트 수정 내용입니다";
    public static final LocalDateTime 댓글_작성일 = LocalDateTime.of(2025,3,25,3,50,32);

    public static Pageable 페이지_요청 = PageRequest.of(0, 10);

    /* Entity */
    public static final Comment 댓글 = new Comment(내용, UserFixtures.회원, PostFixtures.게시글);

    public static final Comment 댓글_생성(User user, Post post) {
        return Comment.builder()
                      .user(user)
                      .post(post)
                      .content(내용)
                      .build();
    }

    /* Request DTO */
    public static final CommentRequest.Enroll 댓글_등록 = new CommentRequest.Enroll(내용);
    public static final CommentRequest.Enroll 잘못된_댓글_등록 = new CommentRequest.Enroll("");

    public static final CommentRequest.Update 댓글_수정 = new CommentRequest.Update(수정_내용);
    public static final CommentRequest.Update 잘못된_댓글_수정 = new CommentRequest.Update("");

    /* Response DTO */
    public static final CommentResponse.Summary 댓글_요약 = new CommentResponse.Summary(
            Id,
            UserFixtures.요약_회원_응답,
            내용,
            댓글_작성일
    );

    public static final CommentResponse.Summary 댓글_수정_요약 = new CommentResponse.Summary(
            Id,
            UserFixtures.요약_회원_응답,
            수정_내용,
            댓글_작성일
    );

    public static final CommentResponse.Detail 댓글_상세 = new CommentResponse.Detail(
            Id,
            PostFixtures.게시글_요약_응답,
            UserFixtures.요약_회원_응답,
            내용,
            댓글_작성일
    );

    public static List<Comment> 댓글_목록 = List.of(댓글, 댓글, 댓글);
    public static Page<Comment> 댓글_페이지 = new PageImpl<>(댓글_목록, 페이지_요청, 댓글_목록.size());
    public static PageResponse<CommentResponse.Summary> 게시글에_달린_댓글_페이지_응답 = new PageResponse<>(댓글_페이지.map(CommentResponse.Summary::from));;
    public static PageResponse<CommentResponse.Detail> 내가_작성한_댓글_페이지_응답 = new PageResponse<>(댓글_페이지.map(CommentResponse.Detail::from));;

}
