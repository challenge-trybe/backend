package com.trybe.moduleapi.post.controller;

import com.trybe.moduleapi.annotation.WithCustomMockUser;
import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.common.ControllerTest;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.post.exception.NotFoundPostException;
import com.trybe.moduleapi.post.fixtures.PostFixtures;
import com.trybe.moduleapi.post.fixtures.PostLikeFixtures;
import com.trybe.moduleapi.post.service.PostLikeService;
import com.trybe.moduleapi.user.fixtures.AuthenticationFixtures;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PostLikeController.class)
class PostLikeControllerTest extends ControllerTest {
    private final String docsPath = "post-like-controller-test/";
    private final String invalidNotFoundPath = "/invalid/not-found/";

    @MockitoBean
    private PostLikeService postLikeService;

    @Test
    @DisplayName("존재하는 게시글에 좋아요를 누르면 200을 반환한다.")
    @WithCustomMockUser
    void 존재하는_게시글에_좋아요를_누르면_200을_반환한다() throws Exception {
        // given
        PostResponse.Like 좋아요_추가_응답 = PostLikeFixtures.좋아요_추가_응답;

        when(postLikeService.addLike(any(User.class), any(Long.class)))
                .thenReturn(좋아요_추가_응답);

        // when
        mockMvc.perform(post("/api/v1/posts/likes/{postId}", PostFixtures.id)
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.likeCount").value(좋아요_추가_응답.likeCount()))
               .andExpect(jsonPath("$.isLiked").value(좋아요_추가_응답.isLiked()))
               .andDo(document(docsPath + "addLike",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("postId").description("게시글 ID")),
                               responseFields(
                                       fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                       fieldWithPath("isLiked").type(JsonFieldType.BOOLEAN).description("좋아요 여부")
                               )
               ));
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 좋아요를 누르면 404을 반환한다")
    @WithCustomMockUser
    void 존재하지_않는_게시글에_좋아요를_누르면_404을_반환한다() throws Exception {
        // given
        doThrow(new NotFoundPostException()).when(postLikeService).addLike(any(User.class), any(Long.class));

        // when
        mockMvc.perform(post("/api/v1/posts/likes/{postId}", PostFixtures.id)
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken))
               .andExpect(status().isNotFound())
               .andExpectAll(
                       jsonPath("$.status").value(404),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               )
               .andDo(document(docsPath + "addLike" + invalidNotFoundPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("postId").description("게시글 ID")),
                               responseFields(
                                       fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                       fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                                       fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가 메시지").optional()
                               )
               ));
    }

    @Test
    @DisplayName("존재하는 게시글에 좋아요를 삭제하면 200을 반환한다")
    @WithCustomMockUser
    void 존재하는_게시글에_좋아요를_삭제하면_200을_반환한다() throws Exception {
        // given
        PostResponse.Like 좋아요_삭제_응답 = PostLikeFixtures.좋아요_삭제_응답;

        when(postLikeService.removeLike(any(User.class), any(Long.class))).thenReturn(좋아요_삭제_응답);

        // when
        mockMvc.perform(delete("/api/v1/posts/likes/{postId}", PostFixtures.id)
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.likeCount").value(좋아요_삭제_응답.likeCount()))
               .andExpect(jsonPath("$.isLiked").value(좋아요_삭제_응답.isLiked()))
               .andDo(document(docsPath + "removeLike",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("postId").description("게시글 ID")),
                               responseFields(
                                       fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                       fieldWithPath("isLiked").type(JsonFieldType.BOOLEAN).description("좋아요 여부")
                               )
               ));
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 좋아요를 삭제하면 404을 반환한다")
    @WithCustomMockUser
    void 존재하지_않는_게시글에_좋아요를_삭제하면_404을_반환한다() throws Exception {
        // given
        doThrow(new NotFoundPostException()).when(postLikeService).removeLike(any(User.class), any(Long.class));

        // when
        mockMvc.perform(delete("/api/v1/posts/likes/{postId}", PostFixtures.id)
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken))
               .andExpect(status().isNotFound())
               .andExpectAll(
                       jsonPath("$.status").value(404),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               )
               .andDo(document(docsPath + "removeLike" + invalidNotFoundPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("postId").description("게시글 ID")),
                               responseFields(
                                       fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                       fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                                       fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가 메시지").optional()
                               )
               ));
    }

    @Test
    @DisplayName("자신이 좋아요 누른 게시글을 조회하면 200을 반환한다.")
    @WithCustomMockUser
    void 자신이_좋아요_누른_게시글을_조회하면_200을_반환한다() throws Exception {
        // given
        PageResponse<PostResponse.Summary> 포스트_페이지_응답 = PostFixtures.컨트롤러_포스트_페이지_응답;
        when(postLikeService.getLikePostByUser(any(User.class), any(Pageable.class))).thenReturn(PostFixtures.컨트롤러_포스트_페이지_응답);

        // when
        mockMvc.perform(get("/api/v1/posts/likes/my")
                                .header("Authorization", "Bearer " + AuthenticationFixtures.accessToken)
                                .param("page", "0")
                                .param("size", "10"))
               .andExpect(status().isOk())
               .andExpectAll(status().isOk(),
                              jsonPath("$.content").isArray(),
                              jsonPath("$.content[0].id").value(포스트_페이지_응답.content().get(0).id()),
                              jsonPath("$.content[0].title").value(포스트_페이지_응답.content().get(0).title()),
                              jsonPath("$.content[0].category").value(포스트_페이지_응답.content().get(0).category().toString()),
                              jsonPath("$.content[0].createdAt").value(포스트_페이지_응답.content().get(0).createdAt().toString()),
                              jsonPath("content[0].writer.id").value(포스트_페이지_응답.content().get(0).writer().id()),
                              jsonPath("content[0].writer.userId").value(포스트_페이지_응답.content().get(0).writer().userId()),
                              jsonPath("content[0].writer.nickname").value(포스트_페이지_응답.content().get(0).writer().nickname()),
                              jsonPath("$.totalElements").value(포스트_페이지_응답.totalElements()),
                              jsonPath("$.totalPages").value(PostFixtures.포스트_페이지_응답.totalPages()),
                              jsonPath("$.size").value(10),
                              jsonPath("$.number").value(0),
                              jsonPath("$.last").value(PostFixtures.포스트_페이지_응답.last()))
               .andDo(document(docsPath + "getLikedPost",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               queryParameters(
                                       parameterWithName("page").description("페이지 번호"),
                                       parameterWithName("size").description("페이지 크기")
                               ),
                               responseFields(
                                       fieldWithPath("content").description("포스트 목록"),
                                       fieldWithPath("content[].id").description("포스트 ID"),
                                       fieldWithPath("content[].title").description("포스트 제목"),
                                       fieldWithPath("content[].category").description("포스트 카테고리"),
                                       fieldWithPath("content[].createdAt").description("포스트 생성일"),
                                       fieldWithPath("content[].writer.id").description("포스트 작성자 ID"),
                                       fieldWithPath("content[].writer.userId").description("포스트 작성자 유저 ID"),
                                       fieldWithPath("content[].writer.nickname").description("포스트 작성자 닉네임"),
                                       fieldWithPath("totalPages").description("총 페이지 수"),
                                       fieldWithPath("totalElements").description("총 요소 수"),
                                       fieldWithPath("size").description("페이지 크기"),
                                       fieldWithPath("number").description("현재 페이지 번호"),
                                       fieldWithPath("last").description("마지막 페이지 여부")
                               )
               ));
    }
}

