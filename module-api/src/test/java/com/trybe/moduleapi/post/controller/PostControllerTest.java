package com.trybe.moduleapi.post.controller;

import com.trybe.moduleapi.annotation.WithCustomMockUser;
import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.challenge.exception.participation.NotFoundChallengeParticipationException;
import com.trybe.moduleapi.common.ControllerTest;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.config.SecurityConfig;
import com.trybe.moduleapi.post.dto.PostRequest;
import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.post.exception.ForbiddenPostException;
import com.trybe.moduleapi.post.exception.NotFoundPostException;
import com.trybe.moduleapi.post.fixtures.PostChallengeFixtures;
import com.trybe.moduleapi.post.fixtures.PostFixtures;
import com.trybe.moduleapi.post.service.PostService;
import com.trybe.moduleapi.user.controller.UserController;
import com.trybe.moduleapi.user.dto.request.UserRequest;
import com.trybe.moduleapi.user.exception.NotFoundUserException;
import com.trybe.moduleapi.user.fixtures.AuthenticationFixtures;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.challenge.enums.ChallengeCategory;
import com.trybe.modulecore.challenge.enums.ChallengeStatus;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PostController.class)
class PostControllerTest extends ControllerTest {
    private String docsPath = "post-controller-test/";
    private final String invalidBadRequestPath = "/invalid/bad-request/";
    private final String invalidNotParticipationPath = "/invalid/not-Participation/";
    private final String invalidNotFoundPath = "/invalid/not-found/";
    private final String invalidForbiddenPath = "/invalid/forbidden/";

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("정상적인 포스트 생성 요청 시 200을 반환한다.")
    @WithCustomMockUser
    void 정상적인_포스트_생성_요청_시_200을_반환한다() throws Exception {
        PostRequest.Create 게시글_생성 = PostFixtures.게시글_생성;
        PostResponse.Detail 게시글_상세_응답 = PostFixtures.컨트롤러_테스트_게시글_상세_응답;
        when(postService.save(any(User.class), any(PostRequest.Create.class))).thenReturn(게시글_상세_응답);

        mockMvc.perform(post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer "+ AuthenticationFixtures.accessToken)
                                .content(objectMapper.writeValueAsString(게시글_생성)))
               .andExpect(status().isOk())
               .andExpectAll(
                       jsonPath("$.id").value(게시글_상세_응답.id()),
                       jsonPath("$.title").value(게시글_상세_응답.title()),
                       jsonPath("$.content").value(게시글_상세_응답.content()),
                       jsonPath("$.category").value(게시글_상세_응답.category().toString()),
                       jsonPath("$.createdAt").value(게시글_상세_응답.createdAt().toString()),
                       jsonPath("$.challengeSummary").isArray(),
                       jsonPath("$.challengeSummary[0].id").value(게시글_상세_응답.challengeSummary().get(0).id()),
                       jsonPath("$.challengeSummary[0].title").value(게시글_상세_응답.challengeSummary().get(0).title()),
                       jsonPath("$.challengeSummary[0].description").value(게시글_상세_응답.challengeSummary().get(0).description()),
                       jsonPath("$.challengeSummary[0].status").value(게시글_상세_응답.challengeSummary().get(0).status().toString()),
                       jsonPath("$.challengeSummary[0].capacity").value(게시글_상세_응답.challengeSummary().get(0).capacity()),
                       jsonPath("$.challengeSummary[0].category").value(게시글_상세_응답.challengeSummary().get(0).category().toString()))
               .andDo(document(docsPath + "save",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("title").type(JsonFieldType.STRING).description("게시글 제목"),
                                       fieldWithPath("content").type(JsonFieldType.STRING).description("게시글 내용"),
                                       fieldWithPath("category").type(JsonFieldType.STRING).description("게시글 카테고리"),
                                       fieldWithPath("challengeId").type(JsonFieldType.ARRAY).description("챌린지 ID").optional()
                               ),
                               responseFields(
                                       fieldWithPath("id").description("게시글 ID"),
                                       fieldWithPath("title").type(JsonFieldType.STRING).description("게시글 내용"),
                                       fieldWithPath("content").type(JsonFieldType.STRING).description("게시글 내용"),
                                       fieldWithPath("category").type(JsonFieldType.STRING).description("게시글 카테고리"),
                                       fieldWithPath("user.id").description("작성자 ID"),
                                       fieldWithPath("user.userId").type(JsonFieldType.STRING).description("작성자 아이디"),
                                       fieldWithPath("user.nickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                       fieldWithPath("createdAt").description("게시글 생성일"), // 날짜 널로 들어감
                                       fieldWithPath("challengeSummary[]").type(JsonFieldType.ARRAY).description("챌린지 요약 내용"),
                                       fieldWithPath("challengeSummary[].id").description("챌린지 ID"), // ID는 널로 들어감
                                       fieldWithPath("challengeSummary[].title").type(JsonFieldType.STRING).description("챌린지 제목"),
                                       fieldWithPath("challengeSummary[].description").type(JsonFieldType.STRING).description("챌린지 설명"),
                                       fieldWithPath("challengeSummary[].status").type(JsonFieldType.STRING).description("챌린지 상태"),
                                       fieldWithPath("challengeSummary[].capacity").type(JsonFieldType.NUMBER).description("챌린지 인원 수"),
                                       fieldWithPath("challengeSummary[].category").type(JsonFieldType.STRING).description("챌린지 카테고리")
                               )
               ));

    }

    @Test
    @DisplayName("유효성 검증에 실패하는 포스트 생성 요청 시 400을 반환한다.")
    @WithCustomMockUser
    void 유효성_검증에_실패하는_포스트_생성_요청_시_400을_반환한다() throws Exception {
        PostRequest.Create 게시글_생성 = PostFixtures.잘못된_게시글_생성;

        mockMvc.perform(post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer "+ AuthenticationFixtures.accessToken)
                                .content(objectMapper.writeValueAsString(게시글_생성)))
               .andExpect(status().isBadRequest())
               .andExpectAll(
                       jsonPath("$.status").value(400),
                       jsonPath("$.message").doesNotExist(),
                       jsonPath("$.data.category").exists(),
                       jsonPath("$.data.title").exists(),
                       jsonPath("$.data.content").exists()
               ).andDo(document(docsPath + "save" + invalidBadRequestPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("title").description("게시글 제목"),
                                       fieldWithPath("content").description("게시글 내용"),
                                       fieldWithPath("category").description("게시글 카테고리"),
                                       fieldWithPath("challengeId").description("챌린지 ID").optional()
                               ),
                               responseFields(
                                       fieldWithPath("status").description("HTTP 상태 코드"),
                                       fieldWithPath("message").description("유효성 검증 오류 메시지"),
                                       fieldWithPath("data.category").description("포스트 카테고리 필드에 대한 유효성 오류 메시지"),
                                       fieldWithPath("data.title").description("제목 필드에 대한 유효성 오류 메시지"),
                                       fieldWithPath("data.content").description("내용 필드에 대한 유효성 오류 메시지")
                               )
               ));
    }

    @Test
    @DisplayName("포스트 생성 시 참여하지 않은 챌린지_ID를 보내면 404을 반환한다.")
    @WithCustomMockUser
    void 포스트_생성_시_참여하지_않은_챌린지_ID를_보내면_404을_반환한다() throws Exception {
        PostRequest.Create 게시글_생성 = PostFixtures.게시글_생성;
        doThrow(new NotFoundChallengeParticipationException("참여하지 않는 챌린지는 언급할 수 없습니다.")).when(postService).save(any(User.class), any(PostRequest.Create.class));

        mockMvc.perform(post("/api/v1/posts")
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer "+ AuthenticationFixtures.accessToken)
                                .content(objectMapper.writeValueAsString(게시글_생성)))
               .andExpect(status().isNotFound())
               .andExpectAll(
                       jsonPath("$.status").value(404),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               ).andDo(document(docsPath + "save" + invalidNotParticipationPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("title").type(JsonFieldType.STRING).description("게시글 제목"),
                                       fieldWithPath("content").type(JsonFieldType.STRING).description("게시글 내용"),
                                       fieldWithPath("category").type(JsonFieldType.STRING).description("게시글 카테고리"),
                                       fieldWithPath("challengeId").type(JsonFieldType.ARRAY).description("챌린지 ID").optional()
                               ),
                               responseFields(
                                       fieldWithPath("status").description("HTTP 상태 코드"),
                                       fieldWithPath("message").description("유효성 검증 오류 메시지"),
                                       fieldWithPath("data").description("상세 메시지")
                               )
               ));

    }

    @Test
    @DisplayName("존재하는 포스트 조회 시 200을 반환한다.")
    @WithCustomMockUser
    void 존재하는_포스트_조회_시_200을_반환한다() throws Exception {
        PostResponse.Detail 게시글_상세_응답 = PostFixtures.컨트롤러_테스트_게시글_상세_응답;
        when(postService.find(any(Long.class))).thenReturn(게시글_상세_응답);

        mockMvc.perform(get("/api/v1/posts/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer "+ AuthenticationFixtures.accessToken))
               .andExpect(status().isOk())
               .andExpectAll(
                       jsonPath("$.id").value(게시글_상세_응답.id()),
                       jsonPath("$.title").value(게시글_상세_응답.title()),
                       jsonPath("$.content").value(게시글_상세_응답.content()),
                       jsonPath("$.category").value(게시글_상세_응답.category().toString()),
                       jsonPath("$.createdAt").exists(), // 이게 왜 존재하지 않지?
                       jsonPath("$.challengeSummary").isArray(),
                       jsonPath("$.challengeSummary[0].id").value(게시글_상세_응답.challengeSummary().get(0).id()),
                       jsonPath("$.challengeSummary[0].title").value(게시글_상세_응답.challengeSummary().get(0).title()),
                       jsonPath("$.challengeSummary[0].description").value(게시글_상세_응답.challengeSummary().get(0).description()),
                       jsonPath("$.challengeSummary[0].status").value(게시글_상세_응답.challengeSummary().get(0).status().toString()),
                       jsonPath("$.challengeSummary[0].capacity").value(게시글_상세_응답.challengeSummary().get(0).capacity()),
                       jsonPath("$.challengeSummary[0].category").value(게시글_상세_응답.challengeSummary().get(0).category().toString()))
               .andDo(document(docsPath + "findById",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("id").description("포스트 ID")),
                               responseFields(
                                       fieldWithPath("id").description("게시글 ID"),
                                       fieldWithPath("title").type(JsonFieldType.STRING).description("게시글 내용"),
                                       fieldWithPath("content").type(JsonFieldType.STRING).description("게시글 내용"),
                                       fieldWithPath("category").type(JsonFieldType.STRING).description("게시글 카테고리"),
                                       fieldWithPath("user.id").description("작성자 ID"),
                                       fieldWithPath("user.userId").type(JsonFieldType.STRING).description("작성자 아이디"),
                                       fieldWithPath("user.nickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                       fieldWithPath("createdAt").description("게시글 생성일"), // 날짜 널로 들어감
                                       fieldWithPath("challengeSummary[]").type(JsonFieldType.ARRAY).description("챌린지 요약 내용"),
                                       fieldWithPath("challengeSummary[].id").description("챌린지 ID"), // ID는 널로 들어감
                                       fieldWithPath("challengeSummary[].title").type(JsonFieldType.STRING).description("챌린지 제목"),
                                       fieldWithPath("challengeSummary[].description").type(JsonFieldType.STRING).description("챌린지 설명"),
                                       fieldWithPath("challengeSummary[].status").type(JsonFieldType.STRING).description("챌린지 상태"),
                                       fieldWithPath("challengeSummary[].capacity").type(JsonFieldType.NUMBER).description("챌린지 인원 수"),
                                       fieldWithPath("challengeSummary[].category").type(JsonFieldType.STRING).description("챌린지 카테고리")
                               )
               ));


    }
    @Test
    @DisplayName("존재하지 않는 포스트 조회 시 404을 반환한다.")
    @WithCustomMockUser
    void 존재하지_않는_포스트_조회_시_404을_반환한다() throws Exception {
        doThrow(new NotFoundPostException()).when(postService).find(eq(1L));
        mockMvc.perform(get("/api/v1/posts/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer "+ AuthenticationFixtures.accessToken))
               .andExpect(status().isNotFound())
               .andExpectAll(
                       jsonPath("$.status").value(404),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               ).andDo(document(docsPath + "findById" + invalidNotFoundPath,
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("id").description("수정할 포스트 ID")),
                                responseFields(
                                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가 메시지").optional()
                                )
               ));
    }

    @Test
    @DisplayName("포스트 페이징 조회 시 200을 반환한다")
    @WithCustomMockUser
    void 포스트_페이징_조회_시_200을_반환한다() throws Exception {
        /* given */
        PostRequest.Read request = PostFixtures.게시글_필터링_조회;
        PageResponse<PostResponse.Summary> 포스트_페이지_응답 = PostFixtures.컨트롤러_포스트_페이지_응답;

        when(postService.findAll(request, PageRequest.of(0, 10)))
                .thenReturn(포스트_페이지_응답);

        /* when */
       mockMvc.perform(post("/api/v1/posts/search")
                               .param("page", "0")
                               .param("size", "10")
                               .param("order", "LATEST")
                               .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                               .header("Authorization", "Bearer "+ AuthenticationFixtures.accessToken)
                               .content(objectMapper.writeValueAsString(request)))
              .andExpectAll(status().isOk(),
                            jsonPath("$.content").isArray(),
                            jsonPath("$.content[0].id").value(포스트_페이지_응답.content().get(0).id()),
                            jsonPath("$.content[0].title").value(포스트_페이지_응답.content().get(0).title()),
                            jsonPath("$.content[0].content").value(포스트_페이지_응답.content().get(0).content()),
                            jsonPath("$.content[0].category").value(포스트_페이지_응답.content().get(0).category().toString()),
                            jsonPath("$.content[0].createdAt").value(포스트_페이지_응답.content().get(0).createdAt().toString()),
                            jsonPath("content[0].user.id").value(포스트_페이지_응답.content().get(0).user().id()),
                            jsonPath("content[0].user.userId").value(포스트_페이지_응답.content().get(0).user().userId()),
                            jsonPath("content[0].user.nickname").value(포스트_페이지_응답.content().get(0).user().nickname()),
                            jsonPath("$.totalElements").value(포스트_페이지_응답.totalElements()),
                            jsonPath("$.totalPages").value(PostFixtures.포스트_페이지_응답.totalPages()),
                            jsonPath("$.size").value(10),
                            jsonPath("$.number").value(0),
                            jsonPath("$.last").value(PostFixtures.포스트_페이지_응답.last()))
              .andDo(document(docsPath + "search",
                              preprocessRequest(prettyPrint()),
                              preprocessResponse(prettyPrint()),
                              queryParameters(
                                      parameterWithName("page").description("페이지 번호"),
                                      parameterWithName("size").description("페이지 크기"),
                                      parameterWithName("order").description("정렬 기준")
                              ),
                              requestFields(
                                      fieldWithPath("keyword").description("포스트 검색 키워트"),
                                      fieldWithPath("categories").description("포스트 카테고리"),
                                      fieldWithPath("order").description("포스트 정렬 기준")
                              ),
                              responseFields(
                                      fieldWithPath("content").description("포스트 목록"),
                                      fieldWithPath("content[].id").description("포스트 ID"),
                                      fieldWithPath("content[].title").description("포스트 제목"),
                                      fieldWithPath("content[].content").description("포스트 내용"),
                                      fieldWithPath("content[].category").description("포스트 카테고리"),
                                      fieldWithPath("content[].createdAt").description("포스트 생성일"),
                                      fieldWithPath("content[].user.id").description("포스트 작성자 ID"),
                                      fieldWithPath("content[].user.userId").description("포스트 작성자 유저 ID"),
                                      fieldWithPath("content[].user.nickname").description("포스트 작성자 닉네임"),
                                      fieldWithPath("totalPages").description("총 페이지 수"),
                                      fieldWithPath("totalElements").description("총 요소 수"),
                                      fieldWithPath("size").description("페이지 크기"),
                                      fieldWithPath("number").description("현재 페이지 번호"),
                                      fieldWithPath("last").description("마지막 페이지 여부")
                              )
        ));

    }

    @Test
    @DisplayName("정상적인 포스트 수정 요청 시 200을 반환한다")
    @WithCustomMockUser
    void 정상적인_포스트_수정_요청_시_200을_반환한다() throws Exception {
        PostRequest.Update 게시글_수정 = PostFixtures.게시글_수정;
        PostResponse.Detail 게시글_상세_응답 = PostFixtures.컨트롤러_테스트_게시글_상세_응답;
        when(postService.updatePost(any(User.class), any(Long.class), any(PostRequest.Update.class))).thenReturn(게시글_상세_응답);

        mockMvc.perform(put("/api/v1/posts/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer "+ AuthenticationFixtures.accessToken)
                                .content(objectMapper.writeValueAsString(게시글_수정)))
               .andExpect(status().isOk())
               .andExpectAll(
                       jsonPath("$.id").value(게시글_상세_응답.id()),
                       jsonPath("$.title").value(게시글_상세_응답.title()),
                       jsonPath("$.content").value(게시글_상세_응답.content()),
                       jsonPath("$.category").value(게시글_상세_응답.category().toString()),
                       jsonPath("$.createdAt").value(게시글_상세_응답.createdAt().toString()),
                       jsonPath("$.challengeSummary").isArray(),
                       jsonPath("$.challengeSummary[0].id").value(게시글_상세_응답.challengeSummary().get(0).id()),
                       jsonPath("$.challengeSummary[0].title").value(게시글_상세_응답.challengeSummary().get(0).title()),
                       jsonPath("$.challengeSummary[0].description").value(게시글_상세_응답.challengeSummary().get(0).description()),
                       jsonPath("$.challengeSummary[0].status").value(게시글_상세_응답.challengeSummary().get(0).status().toString()),
                       jsonPath("$.challengeSummary[0].capacity").value(게시글_상세_응답.challengeSummary().get(0).capacity()),
                       jsonPath("$.challengeSummary[0].category").value(게시글_상세_응답.challengeSummary().get(0).category().toString()))
               .andDo(document(docsPath + "update",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("id").description("포스트 ID")),
                               requestFields(
                                       fieldWithPath("title").type(JsonFieldType.STRING).description("게시글 제목"),
                                       fieldWithPath("content").type(JsonFieldType.STRING).description("게시글 내용"),
                                       fieldWithPath("category").type(JsonFieldType.STRING).description("게시글 카테고리"),
                                       fieldWithPath("challengeId").type(JsonFieldType.ARRAY).description("챌린지 ID").optional()
                               ),
                               responseFields(
                                       fieldWithPath("id").description("게시글 ID"),
                                       fieldWithPath("title").type(JsonFieldType.STRING).description("게시글 내용"),
                                       fieldWithPath("content").type(JsonFieldType.STRING).description("게시글 내용"),
                                       fieldWithPath("category").type(JsonFieldType.STRING).description("게시글 카테고리"),
                                       fieldWithPath("user.id").description("작성자 ID"),
                                       fieldWithPath("user.userId").type(JsonFieldType.STRING).description("작성자 아이디"),
                                       fieldWithPath("user.nickname").type(JsonFieldType.STRING).description("작성자 닉네임"),
                                       fieldWithPath("createdAt").description("게시글 생성일"),
                                       fieldWithPath("challengeSummary[]").type(JsonFieldType.ARRAY).description("챌린지 요약 내용"),
                                       fieldWithPath("challengeSummary[].id").description("챌린지 ID"),
                                       fieldWithPath("challengeSummary[].title").type(JsonFieldType.STRING).description("챌린지 제목"),
                                       fieldWithPath("challengeSummary[].description").type(JsonFieldType.STRING).description("챌린지 설명"),
                                       fieldWithPath("challengeSummary[].status").type(JsonFieldType.STRING).description("챌린지 상태"),
                                       fieldWithPath("challengeSummary[].capacity").type(JsonFieldType.NUMBER).description("챌린지 인원 수"),
                                       fieldWithPath("challengeSummary[].category").type(JsonFieldType.STRING).description("챌린지 카테고리")
                               )
               ));

    }

    @Test
    @DisplayName("유효성 검증에 실패하는 포스트 수정 요청 시 400을 반환한다")
    @WithCustomMockUser
    void 유효성_검증에_실패하는_포스트_수정_요청_시_400을_반환한다() throws Exception {
        PostRequest.Update 게시글_수정 = PostFixtures.잘못된_게시글_수정;
        PostResponse.Detail 게시글_상세_응답 = PostFixtures.컨트롤러_테스트_게시글_상세_응답;
        when(postService.updatePost(any(User.class), any(Long.class), any(PostRequest.Update.class))).thenReturn(게시글_상세_응답);

        mockMvc.perform(put("/api/v1/posts/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer "+ AuthenticationFixtures.accessToken)
                                .content(objectMapper.writeValueAsString(게시글_수정)))
               .andExpect(status().isBadRequest())
               .andExpectAll(
                       jsonPath("$.status").value(400),
                       jsonPath("$.message").doesNotExist(),
                       jsonPath("$.data.category").exists(),
                       jsonPath("$.data.title").exists(),
                       jsonPath("$.data.content").exists()
               ).andDo(document(docsPath + "update" + invalidBadRequestPath,
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("id").description("포스트 ID")),
                                requestFields(
                                        fieldWithPath("title").description("게시글 제목"),
                                        fieldWithPath("content").description("게시글 내용"),
                                        fieldWithPath("category").description("게시글 카테고리"),
                                        fieldWithPath("challengeId").description("챌린지 ID").optional()
                                ),
                                responseFields(
                                        fieldWithPath("status").description("HTTP 상태 코드"),
                                        fieldWithPath("message").description("유효성 검증 오류 메시지"),
                                        fieldWithPath("data.category").description("포스트 카테고리 필드에 대한 유효성 오류 메시지"),
                                        fieldWithPath("data.title").description("제목 필드에 대한 유효성 오류 메시지"),
                                        fieldWithPath("data.content").description("내용 필드에 대한 유효성 오류 메시지")
                                )
               ));

    }

    @Test
    @DisplayName("접근권한이 없는 포스트 수정 요청 시 403을 반환한다")
    @WithCustomMockUser
    void 접근권한이_없는_포스트_수정_요청_시_403을_반환한다() throws Exception {
        PostRequest.Update 게시글_수정 = PostFixtures.게시글_수정;

        doThrow(new ForbiddenPostException()).when(postService).updatePost(any(User.class), eq(1L), any(PostRequest.Update.class));
        mockMvc.perform(put("/api/v1/posts/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer "+ AuthenticationFixtures.accessToken)
                                .content(objectMapper.writeValueAsString(게시글_수정)))
               .andExpect(status().isForbidden())
               .andExpectAll(
                       jsonPath("$.status").value(403),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               ).andDo(document(docsPath + "update" + invalidForbiddenPath,
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("id").description("조회할 포스트 ID")),
                                responseFields(
                                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가 메시지").optional()
                                )
               ));

    }

    @Test
    @DisplayName("존재하지 않는 포스트 수정 요청 시 404을 반환한다")
    @WithCustomMockUser
    void 존재하지_않는_포스트_수정_요청_시_404을_반환한다() throws Exception {
        PostRequest.Update 게시글_수정 = PostFixtures.게시글_수정;

        doThrow(new NotFoundPostException()).when(postService).updatePost(any(User.class), eq(1L), any(PostRequest.Update.class));
        mockMvc.perform(put("/api/v1/posts/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                                .header("Authorization", "Bearer "+ AuthenticationFixtures.accessToken)
                                .content(objectMapper.writeValueAsString(게시글_수정)))
               .andExpect(status().isNotFound())
               .andExpectAll(
                       jsonPath("$.status").value(404),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               ).andDo(document(docsPath + "update" + invalidNotFoundPath,
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("id").description("수정할 포스트 ID")),
                                responseFields(
                                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가 메시지").optional()
                                )
               ));

    }

    @Test
    @DisplayName("포스트 성공적으로 삭제 시 200을 반환한다")
    @WithCustomMockUser
    void 포스트_성공적으로_삭제_시_200을_반환한다() throws Exception {
        doNothing().when(postService).delete(any(User.class), eq(1L));
        mockMvc.perform(delete("/api/v1/posts/{id}", 1L)
                                .header("Authorization", "Bearer "+ AuthenticationFixtures.accessToken))
               .andExpect(status().isOk())
               .andDo(document(docsPath + "delete",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("id").description("조회할 포스트 ID"))
               ));
    }

    @Test
    @DisplayName("접근권한이 없는 포스트 삭제 시 403을 반환한다.")
    @WithCustomMockUser
    void 접근권한이_없는_포스트_삭제_시_403을_반환한다() throws Exception {
        doThrow(new ForbiddenPostException()).when(postService).delete(any(User.class), eq(1L));
        mockMvc.perform(delete("/api/v1/posts/{id}", 1L)
                                .header("Authorization", "Bearer "+ AuthenticationFixtures.accessToken))
               .andExpect(status().isForbidden())
               .andExpectAll(
                       jsonPath("$.status").value(403),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               ).andDo(document(docsPath + "delete" + invalidForbiddenPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               pathParameters(parameterWithName("id").description("조회할 포스트 ID")),
                               responseFields(
                                       fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                       fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                       fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가 메시지").optional()
                               )
               ));
    }

    @Test
    @DisplayName("존재하지 않는 포스트 삭제 시 404을 반환한다")
    @WithCustomMockUser
    void 존재하지_않는_포스트_삭제_시_404을_반환한다() throws Exception {
        doThrow(new NotFoundPostException()).when(postService).delete(any(User.class), eq(1L));
        mockMvc.perform(delete("/api/v1/posts/{id}", 1L)
                                .header("Authorization", "Bearer "+ AuthenticationFixtures.accessToken))
               .andExpect(status().isNotFound())
               .andExpectAll(
                       jsonPath("$.status").value(404),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist()
               ).andDo(document(docsPath + "delete" + invalidNotFoundPath,
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("id").description("조회할 포스트 ID")),
                                responseFields(
                                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("응답 코드"),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("추가 메시지").optional()
                                )
               ));
    }

}
