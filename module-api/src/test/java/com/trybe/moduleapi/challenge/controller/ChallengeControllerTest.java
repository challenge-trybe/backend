package com.trybe.moduleapi.challenge.controller;

import com.trybe.moduleapi.annotation.WithCustomMockUser;
import com.trybe.moduleapi.challenge.dto.ChallengeRequest;
import com.trybe.moduleapi.challenge.exception.InvalidChallengeStatusException;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.challenge.exception.participation.InvalidChallengeRoleActionException;
import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.challenge.service.ChallengeService;
import com.trybe.moduleapi.common.ControllerTest;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChallengeController.class)
class ChallengeControllerTest extends ControllerTest {
    @MockitoBean
    private ChallengeService challengeService;

    private String endpoint = "/api/v1/challenges";

    private String docsPath = "challenge-controller-test/";
    private final String invalidBadRequestPath = "invalid/bad-request/";
    private final String invalidNotFoundPath = "invalid/not-found/";
    private final String invalidForbiddenPath = "invalid/forbidden/";
    private final String invalidConflictPath = "invalid/conflict/";

    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 챌린지 생성 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_생성_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        ChallengeRequest.Create request = ChallengeFixtures.챌린지_생성_요청;

        when(challengeService.save(any(User.class), eq(request)))
                .thenReturn(ChallengeFixtures.챌린지_상세_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.title").value(request.title()),
                jsonPath("$.description").value(request.description()),
                jsonPath("$.startDate").value(request.startDate().toString().toString()),
                jsonPath("$.endDate").value(request.endDate().toString().toString()),
                jsonPath("$.status").value(ChallengeFixtures.대기중.toString()),
                jsonPath("$.capacity").value(request.capacity()),
                jsonPath("$.category").value(request.category().toString().toString()),
                jsonPath("$.proofWay").value(request.proofWay()),
                jsonPath("$.proofCount").value(request.proofCount())
        );

        result.andDo(document(docsPath + "create",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                        fieldWithPath("title").description("챌린지 제목 (최대 100자)"),
                        fieldWithPath("description").description("챌린지 설명 (최대 1,000자)"),
                        fieldWithPath("startDate").description("챌린지 시작일 (현재 날짜 이후)"),
                        fieldWithPath("endDate").description("챌린지 종료일 (현재 날짜 이후, 시작일 이후)"),
                        fieldWithPath("capacity").description("챌린지 인원 (1 ~ 10명)"),
                        fieldWithPath("category").description("챌린지 카테고리"),
                        fieldWithPath("proofWay").description("챌린지 인증 방법 (최대 500자)"),
                        fieldWithPath("proofCount").description("챌린지 인증 횟수 (1 ~ 30회")
                ),
                responseFields(
                        fieldWithPath("id").description("챌린지 ID"),
                        fieldWithPath("title").description("챌린지 제목"),
                        fieldWithPath("description").description("챌린지 설명"),
                        fieldWithPath("startDate").description("챌린지 시작일"),
                        fieldWithPath("endDate").description("챌린지 종료일"),
                        fieldWithPath("status").description("챌린지 상태"),
                        fieldWithPath("capacity").description("챌린지 인원"),
                        fieldWithPath("category").description("챌린지 카테고리"),
                        fieldWithPath("proofWay").description("챌린지 인증 방법"),
                        fieldWithPath("proofCount").description("챌린지 인증 횟수")
                )));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("비정상적인 챌린지 생성 요청 시 응답코드 400을 반환한다.")
    void 비정상적인_챌린지_생성_요청_시_응답코드_400을_반환한다 () throws Exception {
        /* given */
        ChallengeRequest.Create request = ChallengeFixtures.잘못된_챌린지_생성_요청;

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.status").value(400),
                jsonPath("$.message").doesNotExist(),
                jsonPath("$.data.title").exists(),
                jsonPath("$.data.description").exists(),
                jsonPath("$.data.datesAfterToday").exists(),
                jsonPath("$.data.capacity").exists(),
                jsonPath("$.data.proofWay").exists(),
                jsonPath("$.data.proofCount").exists()
        );

        result.andDo(document(docsPath + "create/" + invalidBadRequestPath,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                        fieldWithPath("title").description("챌린지 제목"),
                        fieldWithPath("description").description("챌린지 설명"),
                        fieldWithPath("startDate").description("챌린지 시작일"),
                        fieldWithPath("endDate").description("챌린지 종료일"),
                        fieldWithPath("capacity").description("챌린지 인원"),
                        fieldWithPath("category").description("챌린지 카테고리"),
                        fieldWithPath("proofWay").description("챌린지 인증 방법"),
                        fieldWithPath("proofCount").description("챌린지 인증 횟수")
                ),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data.title").description("챌린지 제목 에러 메시지"),
                        fieldWithPath("data.description").description("챌린지 설명 에러 메시지"),
                        fieldWithPath("data.datesAfterToday").description("날짜 에러 메시지"),
                        fieldWithPath("data.durationLimit").description("챌린지 기간 에러 메시지"),
                        fieldWithPath("data.capacity").description("챌린지 인원 에러 메시지"),
                        fieldWithPath("data.proofWay").description("챌린지 인증 방법 에러 메시지"),
                        fieldWithPath("data.proofCount").description("챌린지 인증 횟수 에러 메시지")
                )));
    }

    @Test
    @DisplayName("정상적인 챌린지 단일 조회 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_단일_조회_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeService.find(challengeId))
                .thenReturn(ChallengeFixtures.챌린지_상세_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{id}", challengeId));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.title").value(ChallengeFixtures.챌린지_상세_응답.title()),
                jsonPath("$.description").value(ChallengeFixtures.챌린지_상세_응답.description()),
                jsonPath("$.startDate").value(ChallengeFixtures.챌린지_상세_응답.startDate().toString()),
                jsonPath("$.endDate").value(ChallengeFixtures.챌린지_상세_응답.endDate().toString()),
                jsonPath("$.status").value(ChallengeFixtures.대기중.toString()),
                jsonPath("$.capacity").value(ChallengeFixtures.챌린지_상세_응답.capacity()),
                jsonPath("$.category").value(ChallengeFixtures.챌린지_상세_응답.category().toString()),
                jsonPath("$.proofWay").value(ChallengeFixtures.챌린지_상세_응답.proofWay()),
                jsonPath("$.proofCount").value(ChallengeFixtures.챌린지_상세_응답.proofCount())
        );

        result.andDo(document(docsPath + "find",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName("id").description("조회할 챌린지 ID")),
                responseFields(
                        fieldWithPath("id").description("챌린지 ID"),
                        fieldWithPath("title").description("챌린지 제목"),
                        fieldWithPath("description").description("챌린지 설명"),
                        fieldWithPath("startDate").description("챌린지 시작일"),
                        fieldWithPath("endDate").description("챌린지 종료일"),
                        fieldWithPath("status").description("챌린지 상태"),
                        fieldWithPath("capacity").description("챌린지 인원"),
                        fieldWithPath("category").description("챌린지 카테고리"),
                        fieldWithPath("proofWay").description("챌린지 인증 방법"),
                        fieldWithPath("proofCount").description("챌린지 인증 횟수")
                )));
    }

    @Test
    @DisplayName("존재하지 않는 챌린지 단일 조회 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지_단일_조회_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;

        doThrow(new NotFoundChallengeException()).when(challengeService).find(challengeId);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{id}", challengeId));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "find/" + invalidNotFoundPath,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )));
    }

    @Test
    @DisplayName("정상적인 챌린지 필터링 조회 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_필터링_조회_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        ChallengeRequest.Read request = ChallengeFixtures.챌린지_조회_요청;

        when(challengeService.findAll(request, ChallengeFixtures.페이지_요청))
                .thenReturn(ChallengeFixtures.챌린지_페이지_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/search")
                .param("page", "0").param("size", "10")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.totalElements").value(ChallengeFixtures.챌린지_페이지_응답.totalElements())
        );

        result.andDo(document(docsPath + "search",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                queryParameters(
                        parameterWithName("page").description("페이지 번호"),
                        parameterWithName("size").description("페이지 크기")
                ),
                requestFields(
                        fieldWithPath("statuses").description("챌린지 상태"),
                        fieldWithPath("categories").description("챌린지 카테고리")
                ),
                responseFields(
                        fieldWithPath("content").description("챌린지 목록"),
                        fieldWithPath("content[].id").description("챌린지 ID"),
                        fieldWithPath("content[].title").description("챌린지 제목"),
                        fieldWithPath("content[].description").description("챌린지 설명"),
                        fieldWithPath("content[].status").description("챌린지 상태"),
                        fieldWithPath("content[].capacity").description("챌린지 인원"),
                        fieldWithPath("content[].category").description("챌린지 카테고리"),
                        fieldWithPath("totalPages").description("총 페이지 수"),
                        fieldWithPath("totalElements").description("총 요소 수"),
                        fieldWithPath("size").description("페이지 크기"),
                        fieldWithPath("number").description("현재 페이지 번호"),
                        fieldWithPath("last").description("마지막 페이지 여부")
                )));
    }

    @Test
    @DisplayName("비정상적인 챌린지 필터링 조회 요청 시 응답코드 400을 반환한다.")
    void 비정상적인_챌린지_필터링_조회_요청_시_응답코드_400을_반환한다 () throws Exception {
        /* given */
        ChallengeRequest.Read request = ChallengeFixtures.잘못된_챌린지_조회_요청;

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/search")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.status").value(400),
                jsonPath("$.message").doesNotExist(),
                jsonPath("$.data.statuses").exists(),
                jsonPath("$.data.categories").exists()
        );

        result.andDo(document(docsPath + "search/" + invalidBadRequestPath,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data.statuses").description("챌린지 상태 에러 메시지"),
                        fieldWithPath("data.categories").description("챌린지 카테고리 에러 메시지")
                )));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 챌린지 내용 수정 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_내용_수정_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateContent request = ChallengeFixtures.챌린지_내용_수정_요청;

        when(challengeService.updateContent(any(User.class), eq(challengeId), eq(request)))
                .thenReturn(ChallengeFixtures.내용_수정된_챌린지_상세_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{id}/content", challengeId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.title").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.title()),
                jsonPath("$.description").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.description()),
                jsonPath("$.startDate").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.startDate().toString().toString()),
                jsonPath("$.endDate").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.endDate().toString()),
                jsonPath("$.status").value(ChallengeFixtures.대기중.toString()),
                jsonPath("$.capacity").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.capacity()),
                jsonPath("$.category").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.category().toString()),
                jsonPath("$.proofWay").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.proofWay()),
                jsonPath("$.proofCount").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.proofCount())
        );

        result.andDo(document(docsPath + "update-content",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName("id").description("수정할 챌린지 ID")),
                requestFields(
                        fieldWithPath("title").description("챌린지 제목 (최대 100자)"),
                        fieldWithPath("description").description("챌린지 설명 (최대 1,000자)"),
                        fieldWithPath("startDate").description("챌린지 시작일 (현재 날짜 이후)"),
                        fieldWithPath("endDate").description("챌린지 종료일 (현재 날짜 이후, 시작일 이후)"),
                        fieldWithPath("capacity").description("챌린지 인원 (1 ~ 10명)"),
                        fieldWithPath("category").description("챌린지 카테고리")
                ),
                responseFields(
                        fieldWithPath("id").description("챌린지 ID"),
                        fieldWithPath("title").description("챌린지 제목"),
                        fieldWithPath("description").description("챌린지 설명"),
                        fieldWithPath("startDate").description("챌린지 시작일"),
                        fieldWithPath("endDate").description("챌린지 종료일"),
                        fieldWithPath("status").description("챌린지 상태"),
                        fieldWithPath("capacity").description("챌린지 인원"),
                        fieldWithPath("category").description("챌린지 카테고리"),
                        fieldWithPath("proofWay").description("챌린지 인증 방법"),
                        fieldWithPath("proofCount").description("챌린지 인증 횟수")
                )));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("비정상적인 챌린지 내용 수정 요청 시 응답코드 400을 반환한다.")
    void 비정상적인_챌린지_내용_수정_요청_시_응답코드_400을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateContent request = ChallengeFixtures.잘못된_챌린지_내용_수정_요청;

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{id}/content", challengeId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.status").value(400),
                jsonPath("$.message").doesNotExist(),
                jsonPath("$.data.title").exists(),
                jsonPath("$.data.description").exists(),
                jsonPath("$.data.datesAfterToday").exists(),
                jsonPath("$.data.durationLimit").exists(),
                jsonPath("$.data.capacity").exists()
        );

        result.andDo(document(docsPath + "update-content/" + invalidBadRequestPath,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                        fieldWithPath("title").description("챌린지 제목"),
                        fieldWithPath("description").description("챌린지 설명"),
                        fieldWithPath("startDate").description("챌린지 시작일"),
                        fieldWithPath("endDate").description("챌린지 종료일"),
                        fieldWithPath("capacity").description("챌린지 인원"),
                        fieldWithPath("category").description("챌린지 카테고리")
                ),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data.title").description("챌린지 제목 에러 메시지"),
                        fieldWithPath("data.description").description("챌린지 설명 에러 메시지"),
                        fieldWithPath("data.datesAfterToday").description("날짜 에러 메시지"),
                        fieldWithPath("data.durationLimit").description("챌린지 기간 에러 메시지"),
                        fieldWithPath("data.capacity").description("챌린지 인원 에러 메시지")
                )));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("리더가 아닌 사용자가 챌린지 내용 수정 요청 시 응답코드 403을 반환한다.")
    void 리더가_아닌_사용자가_챌린지_내용_수정_요청_시_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateContent request = ChallengeFixtures.챌린지_내용_수정_요청;

        doThrow(new InvalidChallengeRoleActionException("리더만 챌린지 정보를 수정할 수 있습니다."))
                .when(challengeService).updateContent(any(User.class), eq(challengeId), eq(request));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{id}/content", challengeId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "update-content/" + invalidForbiddenPath,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("진행 예정이 아닌 챌린지 내용 수정 요청 시 응답코드 409을 반환한다.")
    void 진행_예정이_아닌_챌린지_내용_수정_요청_시_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateContent request = ChallengeFixtures.챌린지_내용_수정_요청;

        doThrow(new InvalidChallengeStatusException("진행 예정인 챌린지만 수정할 수 있습니다."))
                .when(challengeService).updateContent(any(User.class), eq(challengeId), eq(request));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{id}/content", challengeId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "update-content/" + invalidConflictPath,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 챌린지 내용 수정 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지_내용_수정_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;
        ChallengeRequest.UpdateContent request = ChallengeFixtures.챌린지_내용_수정_요청;

        doThrow(new NotFoundChallengeException()).when(challengeService).updateContent(any(User.class), eq(challengeId), eq(request));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{id}/content", challengeId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "update-content/" + invalidNotFoundPath,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 챌린지 인증 내용 수정 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_인증_내용_수정_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateProof request = ChallengeFixtures.챌린지_인증_내용_수정_요청;

        when(challengeService.updateProof(any(User.class), eq(challengeId), eq(request)))
                .thenReturn(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{id}/proof", challengeId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.title").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.title()),
                jsonPath("$.description").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.description()),
                jsonPath("$.startDate").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.startDate().toString()),
                jsonPath("$.endDate").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.endDate().toString()),
                jsonPath("$.status").value(ChallengeFixtures.대기중.toString()),
                jsonPath("$.capacity").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.capacity()),
                jsonPath("$.category").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.category().toString()),
                jsonPath("$.proofWay").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.proofWay()),
                jsonPath("$.proofCount").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.proofCount())
        );

        result.andDo(document(docsPath + "update-proof",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName("id").description("수정할 챌린지 ID")),
                requestFields(
                        fieldWithPath("proofWay").description("챌린지 인증 방법 (최대 500자)"),
                        fieldWithPath("proofCount").description("챌린지 인증 횟수 (1 ~ 30회)")
                ),
                responseFields(
                        fieldWithPath("id").description("챌린지 ID"),
                        fieldWithPath("title").description("챌린지 제목"),
                        fieldWithPath("description").description("챌린지 설명"),
                        fieldWithPath("startDate").description("챌린지 시작일"),
                        fieldWithPath("endDate").description("챌린지 종료일"),
                        fieldWithPath("status").description("챌린지 상태"),
                        fieldWithPath("capacity").description("챌린지 인원"),
                        fieldWithPath("category").description("챌린지 카테고리"),
                        fieldWithPath("proofWay").description("챌린지 인증 방법"),
                        fieldWithPath("proofCount").description("챌린지 인증 횟수")
                )));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("비정상적인 챌린지 인증 내용 수정 요청 시 응답코드 400을 반환한다.")
    void 비정상적인_챌린지_인증_내용_수정_요청_시_응답코드_400을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateProof request = ChallengeFixtures.잘못된_챌린지_인증_내용_수정_요청;

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{id}/proof", challengeId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.status").value(400),
                jsonPath("$.message").doesNotExist(),
                jsonPath("$.data.proofWay").exists(),
                jsonPath("$.data.proofCount").exists()
        );

        result.andDo(document(docsPath + "update-proof/" + invalidBadRequestPath,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                        fieldWithPath("proofWay").description("챌린지 인증 방법"),
                        fieldWithPath("proofCount").description("챌린지 인증 횟수")
                ),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data.proofWay").description("챌린지 인증 방법 에러 메시지"),
                        fieldWithPath("data.proofCount").description("챌린지 인증 횟수 에러 메시지")
                )));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("리더가 아닌 사용자가 챌린지 인증 내용 수정 요청 시 응답코드 403을 반환한다.")
    void 리더가_아닌_사용자가_챌린지_인증_내용_수정_요청_시_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateProof request = ChallengeFixtures.챌린지_인증_내용_수정_요청;

        doThrow(new InvalidChallengeRoleActionException("리더만 챌린지 인증 내용을 수정할 수 있습니다."))
                .when(challengeService).updateProof(any(User.class), eq(challengeId), eq(request));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{id}/proof", challengeId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "update-proof/" + invalidForbiddenPath,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("진행 예정이 아닌 챌린지 인증 내용 수정 요청 시 응답코드 409을 반환한다.")
    void 진행_예정이_아닌_챌린지_인증_내용_수정_요청_시_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateProof request = ChallengeFixtures.챌린지_인증_내용_수정_요청;

        doThrow(new InvalidChallengeStatusException("진행 예정인 챌린지만 인증 내용을 수정할 수 있습니다."))
                .when(challengeService).updateProof(any(User.class), eq(challengeId), eq(request));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{id}/proof", challengeId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "update-proof/" + invalidConflictPath,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 챌린지 인증 내용 수정 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지_인증_내용_수정_요청_시_응답코드_404를_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;
        ChallengeRequest.UpdateProof request = ChallengeFixtures.챌린지_인증_내용_수정_요청;

        doThrow(new NotFoundChallengeException()).when(challengeService).updateProof(any(User.class), eq(challengeId), eq(request));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{id}/proof", challengeId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "update-proof/" + invalidNotFoundPath,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("챌린지 삭제 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_삭제_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{id}", challengeId));

        result.andExpect(status().isOk());
        verify(challengeService, atLeastOnce()).delete(any(User.class), eq(challengeId));

        result.andDo(document(docsPath + "delete",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName("id").description("삭제할 챌린지 ID"))));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("리더가 아닌 사용자가 챌린지 삭제 요청 시 응답코드 403을 반환한다.")
    void 리더가_아닌_사용자가_챌린지_삭제_요청_시_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        doThrow(new InvalidChallengeRoleActionException("리더만 챌린지를 삭제할 수 있습니다."))
                .when(challengeService).delete(any(User.class), eq(challengeId));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{id}", challengeId));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "delete/" + invalidForbiddenPath,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("진행 중인 챌린지 삭제 요청 시 응답코드 409을 반환한다.")
    void 진행_중인_챌린지_삭제_요청_시_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        doThrow(new InvalidChallengeStatusException("진행 중인 챌린지는 삭제할 수 없습니다."))
                .when(challengeService).delete(any(User.class), eq(challengeId));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{id}", challengeId));

        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "delete/" + invalidConflictPath,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 챌린지 삭제 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지_삭제_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;

        doThrow(new NotFoundChallengeException()).when(challengeService).delete(any(User.class), eq(challengeId));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{id}", challengeId));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "delete/" + invalidNotFoundPath,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )));
    }
}