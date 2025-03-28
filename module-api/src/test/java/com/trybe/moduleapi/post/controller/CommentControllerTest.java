package com.trybe.moduleapi.post.controller;

import com.trybe.moduleapi.annotation.WithCustomMockUser;
import com.trybe.moduleapi.common.ControllerTest;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.CommentRequest;
import com.trybe.moduleapi.post.dto.CommentResponse;
import com.trybe.moduleapi.post.exception.ForbiddenCommentException;
import com.trybe.moduleapi.post.exception.NotFoundCommentException;
import com.trybe.moduleapi.post.exception.NotFoundPostException;
import com.trybe.moduleapi.post.fixtures.CommentFixtures;
import com.trybe.moduleapi.post.service.CommentService;
import com.trybe.moduleapi.user.fixtures.AuthenticationFixtures;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerTest extends ControllerTest {
    private final String docsPath = "comment-controller-test/";

    @MockitoBean
    private CommentService commentService;

    @Test
    @WithCustomMockUser
    @DisplayName("게시글에 댓글을 성공적으로 작성 시 200을 반환한다.")
    void 게시글에_댓글을_성공적으로_작성_시_200을_반환한다 () throws Exception {
        /* given */
        CommentRequest.Enroll 댓글_등록 = CommentFixtures.댓글_등록;
        CommentResponse.Summary 댓글_응답 = CommentFixtures.댓글_요약;
        when(commentService.enroll(any(User.class), any(Long.class), any(CommentRequest.Enroll.class))).thenReturn(댓글_응답);

        /* when */
        mockMvc.perform(post("/api/v1/posts/{postId}/comments", 1L)
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken)
                                .content(objectMapper.writeValueAsString(댓글_등록)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(댓글_응답.id()))
               .andExpect(jsonPath("$.writer.id").value(댓글_응답.writer().id()))
               .andExpect(jsonPath("$.writer.userId").value(댓글_응답.writer().userId()))
               .andExpect(jsonPath("$.writer.nickname").value(댓글_응답.writer().nickname()))
               .andExpect(jsonPath("$.content").value(댓글_응답.content()))
               .andExpect(jsonPath("$.createdAt").value(댓글_응답.createdAt().toString()))
               .andDo(document(docsPath + "add",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("postId").description("게시글 ID")),
                               requestFields(
                                       fieldWithPath("content").type(JsonFieldType.STRING).description("댓글 내용")
                               ),
                               responseFields(
                                       fieldWithPath("id").description("댓글 ID"),
                                       fieldWithPath("writer.id").description("작성자 ID"),
                                       fieldWithPath("writer.userId").type(JsonFieldType.STRING).description("작성자 아이디"),
                                       fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                       fieldWithPath("content").description("댓글 내용"),
                                       fieldWithPath("createdAt").description("댓글 작성일"))
               ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("유효성 검증에 실패하는 댓글 작성 시 400을 반환한다.")
    void 유효성_검증에_실패하는_댓글_작성_시_400을_반환한다 () throws Exception {
        /* given */
        CommentRequest.Enroll 잘못된_댓글_등록 = CommentFixtures.잘못된_댓글_등록;

        /* when */
        mockMvc.perform(post("/api/v1/posts/{postId}/comments", 1L)
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken)
                                .content(objectMapper.writeValueAsString(잘못된_댓글_등록)))
               .andExpect(status().isBadRequest())
               .andExpectAll(
                       jsonPath("$.status").value(400),
                       jsonPath("$.message").doesNotExist(),
                       jsonPath("$.data.content").exists()
               )
               .andDo(document(docsPath + "add" + invalidBadRequestPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("postId").description("게시글 ID")),
                               requestFields(
                                       fieldWithPath("content").description("댓글 내용")
                               ),
                               responseFields(
                                       fieldWithPath("status").description("HTTP 상태 코드"),
                                       fieldWithPath("message").description("에러 메시지"),
                                       fieldWithPath("data.content").description("댓글 내용 필드에 대한 유효성 오류 메시지")
                               )
               ));
        
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("내가 작성한 댓글 목록 조회 시 200을 반환한다.")
    void 내가_작성한_댓글_목록_조회_시_200을_반환한다 () throws Exception {
        /* given */
        PageResponse<CommentResponse.Detail> 댓글상세페이지응답 = CommentFixtures.내가_작성한_댓글_페이지_응답;
        when(commentService.findAllByUser(any(User.class), any(Pageable.class))).thenReturn(댓글상세페이지응답);

        /* when */
        mockMvc.perform(get("/api/v1/comments/my")
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content").isArray())
               .andExpect(jsonPath("$.content[0].id").value(댓글상세페이지응답.content().get(0).id()))
               .andExpect(jsonPath("$.content[0].content").value(댓글상세페이지응답.content().get(0).content()))
               .andExpect(jsonPath("$.content[0].writer.id").value(댓글상세페이지응답.content().get(0).writer().id()))
               .andExpect(jsonPath("$.content[0].writer.userId").value(댓글상세페이지응답.content().get(0).writer().userId()))
               .andExpect(jsonPath("$.content[0].writer.nickname").value(댓글상세페이지응답.content().get(0).writer().nickname()))
               .andExpect(jsonPath("$.content[0].post.id").value(댓글상세페이지응답.content().get(0).post().id()))
               .andExpect(jsonPath("$.content[0].post.title").value(댓글상세페이지응답.content().get(0).post().title()))
               .andExpect(jsonPath("$.content[0].post.writer.id").value(댓글상세페이지응답.content().get(0).post().writer().id()))
               .andExpect(jsonPath("$.content[0].post.writer.userId").value(댓글상세페이지응답.content().get(0).post().writer().userId()))
               .andExpect(jsonPath("$.content[0].post.writer.nickname").value(댓글상세페이지응답.content().get(0).post().writer().nickname()))
               .andExpect(jsonPath("$.content[0].post.category").value(댓글상세페이지응답.content().get(0).post().category().toString()))
               .andExpect(jsonPath("$.content[0].post.createdAt").value(댓글상세페이지응답.content().get(0).post().createdAt()))
               .andExpect(jsonPath("$.content[0].createdAt").value(댓글상세페이지응답.content().get(0).createdAt()))
               .andDo(document(docsPath + "my",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               responseFields(
                                       fieldWithPath("content[].id").description("댓글 ID"),
                                       fieldWithPath("content[].content").description("댓글 내용"),
                                       fieldWithPath("content[].writer.id").description("댓글 작성자 ID"),
                                       fieldWithPath("content[].writer.userId").description("댓글 작성자의 User ID"),
                                       fieldWithPath("content[].writer.nickname").description("댓글 작성자의 닉네임"),
                                       fieldWithPath("content[].post.id").description("게시글 ID"),
                                       fieldWithPath("content[].post.title").description("게시글 제목"),
                                       fieldWithPath("content[].post.writer.id").description("게시글 작성자 ID"),
                                       fieldWithPath("content[].post.writer.userId").description("게시글 작성자 User ID"),
                                       fieldWithPath("content[].post.writer.nickname").description("게시글 작성자 닉네임"),
                                       fieldWithPath("content[].post.category").description("게시글 카테고리"),
                                       fieldWithPath("content[].post.createdAt").description("게시글 작성일자"),
                                       fieldWithPath("content[].createdAt").description("댓글 작성일자"),
                                       fieldWithPath("totalPages").description("총 페이지 수"),
                                       fieldWithPath("totalElements").description("총 댓글 수"),
                                       fieldWithPath("size").description("페이지당 댓글 수"),
                                       fieldWithPath("number").description("현재 페이지 번호"),
                                       fieldWithPath("last").description("마지막 페이지 여부")
                               )
               ));
    }
    
    @Test
    @DisplayName("게시글에 대한 댓글 목록 조회 시 200을 반환한다.")
    void 게시글에_대한_댓글_목록_조회_시_200을_반환한다 () throws Exception {
        /* given */
        PageResponse<CommentResponse.Summary> 게시글에달린댓글페이지응답 = CommentFixtures.게시글에_달린_댓글_페이지_응답;
        when(commentService.findAllByPost(any(Long.class), any(Pageable.class))).thenReturn(게시글에달린댓글페이지응답);

        /* when */
        mockMvc.perform(get("/api/v1/posts/{postId}/comments", 1L))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content").isArray())
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.totalElements").value(3))
               .andExpect(jsonPath("$.size").value(10))
               .andExpect(jsonPath("$.number").value(0))
               .andExpect(jsonPath("$.last").value(true))
               .andDo(document(docsPath + "find",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("postId").description("게시글 ID")),
                               responseFields(
                                       fieldWithPath("content[]").description("댓글 목록"),
                                       fieldWithPath("content[].id").description("댓글 ID"),
                                       fieldWithPath("content[].content").description("댓글 내용"),
                                       fieldWithPath("content[].writer.id").description("작성자 ID"),
                                       fieldWithPath("content[].writer.userId").description("작성자 사용자 ID"),
                                       fieldWithPath("content[].writer.nickname").description("작성자 닉네임"),
                                       fieldWithPath("content[].createdAt").description("댓글 작성 시간").optional(),
                                       fieldWithPath("totalPages").description("전체 페이지 수"),
                                       fieldWithPath("totalElements").description("전체 댓글 수"),
                                       fieldWithPath("size").description("페이지당 댓글 수"),
                                       fieldWithPath("number").description("현재 페이지 번호"),
                                       fieldWithPath("last").description("마지막 페이지 여부")
                               )
               ));
    }
    
    @Test
    @DisplayName("존재하지 않는 게시글에 대한 댓글 목록 조회 시 404을 반환한다.")
    void 존재하지_않는_게시글에_대한_댓글_목록_조회_시_404을_반환한다 () throws Exception {
        /* given */
        doThrow(new NotFoundPostException()).when(commentService).findAllByPost(eq(999L), any(Pageable.class));

        /* when */
        mockMvc.perform(get("/api/v1/posts/{postId}/comments", 999L))
               .andExpect(status().isNotFound())
               .andExpectAll(
                       jsonPath("$.status").value(404),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               )
               .andDo(document(docsPath + "find" + invalidNotFoundPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("postId").description("존재하지 않는 게시글 ID")),
                               responseFields(
                                       fieldWithPath("status").description("HTTP 상태 코드"),
                                       fieldWithPath("message").description("에러 메시지"),
                                       fieldWithPath("data").description("에러 데이터").optional()
                               )
               ));
        
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 댓글 수정 시 200을 반환한다")
    void 정상적인_댓글_수정_시_200을_반환한다() throws Exception {
        /* given */
        CommentRequest.Update 댓글_수정_요청 = CommentFixtures.댓글_수정;
        CommentResponse.Summary 댓글_수정_응답 = CommentFixtures.댓글_요약;
        when(commentService.update(any(User.class), any(Long.class), any(CommentRequest.Update.class))).thenReturn(댓글_수정_응답);

        /* when */
        mockMvc.perform(put("/api/v1/comments/{commentId}", 1L)
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken)
                                .content(objectMapper.writeValueAsString(댓글_수정_요청)))
               .andExpect(jsonPath("$.id").value(댓글_수정_응답.id()))
               .andExpect(jsonPath("$.writer.id").value(댓글_수정_응답.writer().id()))
               .andExpect(jsonPath("$.writer.userId").value(댓글_수정_응답.writer().userId()))
               .andExpect(jsonPath("$.writer.nickname").value(댓글_수정_응답.writer().nickname()))
               .andExpect(jsonPath("$.content").value(댓글_수정_응답.content()))
               .andExpect(jsonPath("$.createdAt").value(댓글_수정_응답.createdAt().toString()))
               .andDo(document(docsPath + "update",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("commentId").description("댓글 ID")),
                               requestFields(
                                       fieldWithPath("content").type(JsonFieldType.STRING).description("댓글 내용")
                               ),
                               responseFields(
                                       fieldWithPath("id").description("댓글 ID"),
                                       fieldWithPath("writer.id").description("작성자 ID"),
                                       fieldWithPath("writer.userId").type(JsonFieldType.STRING).description("작성자 아이디"),
                                       fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                       fieldWithPath("content").description("댓글 내용"),
                                       fieldWithPath("createdAt").description("댓글 작성일"))
               ));
        
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("유효성 검증에 실패하는 댓글 수정 요청 시 400을 반환한다")
    void 유효성_검증에_실패하는_댓글_수정_요청_시_400을_반환한다 () throws Exception {
        /* given */
        CommentRequest.Update 잘못된_댓글_수정_요청 = CommentFixtures.잘못된_댓글_수정;

        /* when */
        mockMvc.perform(put("/api/v1/comments/{commentId}", 1L)
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken)
                                .content(objectMapper.writeValueAsString(잘못된_댓글_수정_요청)))
               .andExpect(status().isBadRequest())
               .andExpectAll(
                       jsonPath("$.status").value(400),
                       jsonPath("$.message").doesNotExist(),
                       jsonPath("$.data.content").exists()
               )
               .andDo(document(docsPath + "update" + invalidBadRequestPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("commentId").description("댓글 ID")),
                               requestFields(
                                       fieldWithPath("content").description("댓글 내용")
                               ),
                               responseFields(
                                       fieldWithPath("status").description("HTTP 상태 코드"),
                                       fieldWithPath("message").description("에러 메시지"),
                                       fieldWithPath("data.content").description("댓글 내용 필드에 대한 유효성 오류 메시지")
                               )
               ));
        
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 댓글 수정 요청 시 404을 반환한다")
    void 존재하지_않는_댓글_수정_요청_시_404을_반환한다 () throws Exception {
        /* given */
        doThrow(new NotFoundCommentException()).when(commentService).update(any(User.class), eq(999L), any(CommentRequest.Update.class));
        CommentRequest.Update 댓글_수정 = CommentFixtures.댓글_수정;

        /* when */
        mockMvc.perform(put("/api/v1/comments/{commentId}", 999L)
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken)
                                .content(objectMapper.writeValueAsString(댓글_수정)))
               .andExpect(status().isNotFound())
               .andExpectAll(
                       jsonPath("$.status").value(404),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               )
               .andDo(document(docsPath + "update" + invalidNotFoundPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("commentId").description("존재하지 않는 댓글 ID")),
                               requestFields(
                                       fieldWithPath("content").description("댓글 내용")
                               ),
                               responseFields(
                                       fieldWithPath("status").description("HTTP 상태 코드"),
                                       fieldWithPath("message").description("에러 메시지"),
                                       fieldWithPath("data").description("에러 데이터").optional()
                               )
               ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("접근권한이 없는 댓글에 대한 수정 요청 시 403을 반환한다.")
    void 접근권한이_없는_댓글에_대한_수정_요청_시_403을_반환한다 () throws Exception {
        /* given */
        CommentRequest.Update 댓글_수정_요청 = CommentFixtures.댓글_수정;

        doThrow(new ForbiddenCommentException("본인이 작성한 댓글만 수정할 수 있습니다.")).when(commentService).update(any(User.class), eq(1L), any(CommentRequest.Update.class));

        /* when */
        mockMvc.perform(put("/api/v1/comments/{commentId}", 1L)
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken)
                                .content(objectMapper.writeValueAsString(댓글_수정_요청)))
               .andExpect(status().isForbidden())
               .andExpectAll(
                       jsonPath("$.status").value(403),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               )
               .andDo(document(docsPath + "update" + invalidForbiddenPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("commentId").description("댓글 ID")),
                               responseFields(
                                       fieldWithPath("status").description("HTTP 상태 코드"),
                                       fieldWithPath("message").description("에러 메시지"),
                                       fieldWithPath("data").description("에러")
                               )
               ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하는 게시글에 대한 댓글 삭제 요청 시 200을 반환한다.")
    void 존재하는_게시글에_대한_댓글_삭제_요청_시_200을_반환한다() throws Exception {
        /* given */
        doNothing().when(commentService).delete(any(User.class), eq(1L));

        /* when */
        mockMvc.perform(delete("/api/v1/comments/{commentId}", 1L)
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken))
               .andExpect(status().isOk())
               .andDo(document(docsPath + "delete",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("commentId").description("댓글 ID"))
               ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 게시글에 대한 댓글 삭제 요청 시 404을 반환한다.")
    void 존재하지_않는_게시글에_대한_댓글_삭제_요청_시_404을_반환한다() throws Exception {
        /* given */
        doThrow(new NotFoundPostException()).when(commentService).delete(any(User.class), eq(999L));

        /* when */
        mockMvc.perform(delete("/api/v1/comments/{commentId}", 999L)
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken))
               .andExpect(status().isNotFound())
               .andExpectAll(
                       jsonPath("$.status").value(404),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               )
               .andDo(document(docsPath + "delete" + invalidNotFoundPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("commentId").description("댓글 ID")),
                               responseFields(
                                       fieldWithPath("status").description("HTTP 상태 코드"),
                                       fieldWithPath("message").description("에러 메시지"),
                                       fieldWithPath("data").description("에러 데이터").optional()
                               )
               ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("접근권한이 없는 댓글에 대한 삭제 요청 시 403을 반환한다.")
    void 접근권한이_없는_댓글에_대한_삭제_요청_시_403을_반환한다() throws Exception {
        /* given */
        doThrow(new ForbiddenCommentException("본인이 작성한 댓글만 삭제할 수 있습니다.")).when(commentService).delete(any(User.class), eq(1L));

        /* when */
        mockMvc.perform(delete("/api/v1/comments/{commentId}", 1L)
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken))
               .andExpect(status().isForbidden())
               .andExpectAll(
                       jsonPath("$.status").value(403),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               )
               .andDo(document(docsPath + "delete" + invalidForbiddenPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("commentId").description("댓글 ID")),
                               responseFields(
                                       fieldWithPath("status").description("HTTP 상태 코드"),
                                       fieldWithPath("message").description("에러 메시지"),
                                       fieldWithPath("data").description("에러 데이터").optional()
                               )
               ));
    }
}
