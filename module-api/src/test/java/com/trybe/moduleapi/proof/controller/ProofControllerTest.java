package com.trybe.moduleapi.proof.controller;

import com.trybe.moduleapi.annotation.WithCustomMockUser;
import com.trybe.moduleapi.challenge.exception.InvalidChallengeStatusException;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.challenge.exception.participation.InvalidChallengeRoleActionException;
import com.trybe.moduleapi.challenge.exception.participation.InvalidParticipationStatusActionException;
import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.common.ControllerTest;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.proof.dto.request.ProofRequest;
import com.trybe.moduleapi.proof.dto.response.ProofResponse;
import com.trybe.moduleapi.proof.exception.DuplicatedProofException;
import com.trybe.moduleapi.proof.exception.InvalidProofDeletionException;
import com.trybe.moduleapi.proof.exception.NotFoundProofException;
import com.trybe.moduleapi.proof.exception.ProofCountExceededException;
import com.trybe.moduleapi.proof.service.ProofService;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;

import static com.trybe.moduleapi.proof.fixtures.ProofFixtures.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProofController.class)
class ProofControllerTest extends ControllerTest {
    @MockitoBean
    private ProofService proofService;

    private final String endpoint = "/api/v1/proofs";

    private final String docsPath = "proof-controller-test/";
    private final String invalidBadRequestPath = "/invalid/bad-request/";
    private final String invalidNotFoundPath = "/invalid/not-found/";
    private final String invalidConflictPath = "/invalid/conflict/";
    private final String invalidForbiddenPath = "/invalid/forbidden/";

    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 인증 생성 요청 시 응답코드 200을 반환한다.")
    void 정상적인_인증_생성_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        ProofRequest.Create request = 인증_생성_요청;
        ProofResponse.Summary response = 인증_요약_응답;

        when(proofService.save(any(User.class), any(ProofRequest.Create.class)))
                .thenReturn(response);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(response.id()),
                jsonPath("$.date").value(response.date().toString()),
                jsonPath("$.round").value(response.round())
        );

        result.andDo(document(docsPath + "save",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                        fieldWithPath("challengeId").description("챌린지 ID"),
                        fieldWithPath("date").description("인증 수행 날짜")
                ),
                responseFields(
                        fieldWithPath("id").description("인증 ID"),
                        fieldWithPath("date").description("인증 수행 날짜"),
                        fieldWithPath("round").description("인증 라운드")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("비정상적인 인증 생성 요청 시 응답코드 400을 반환한다.")
    void 비정상적인_인증_생성_요청_시_응답코드_400을_반환한다 () throws Exception {
        /* given */
        ProofRequest.Create request = 잘못된_날짜_인증_생성_요청;

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.status").value(400),
                jsonPath("$.message").doesNotExist(),
                jsonPath("$.data.date").exists()
        );

        result.andDo(document(docsPath + "save" + invalidBadRequestPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        subsectionWithPath("data").description("에러 데이터"),
                        fieldWithPath("data.date").description("인증 수행 날짜 에러")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 챌린지에 대한 인증 생성 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지에_대한_인증_생성_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        ProofRequest.Create request = 인증_생성_요청;

        when(proofService.save(any(User.class), any(ProofRequest.Create.class)))
                .thenThrow(new NotFoundProofException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "save" + invalidNotFoundPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("에러 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("인증 생성 요청 시 주어진 챌린지의 리더가 아닌 경우 응답코드 403을 반환한다.")
    void 인증_생성_요청_시_주어진_챌린지의_리더가_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        ProofRequest.Create request = 인증_생성_요청;

        when(proofService.save(any(User.class), any(ProofRequest.Create.class)))
                .thenThrow(new InvalidChallengeRoleActionException("리더만 인증을 등록할 수 있습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "save" + invalidForbiddenPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("에러 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("인증 생성 요청 시 진행 중인 챌린지가 아닌 경우 응답코드 409을 반환한다.")
    void 인증_생성_요청_시_진행_중인_챌린지가_아닌_경우_응답코드_409을_반환한다 () throws Exception {
        /* given */
        ProofRequest.Create request = 인증_생성_요청;

        when(proofService.save(any(User.class), any(ProofRequest.Create.class)))
                .thenThrow(new InvalidChallengeStatusException("진행 중인 챌린지만 인증을 등록할 수 있습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "save" + invalidConflictPath + "status",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("에러 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("인증 생성 요청 시 중복된 인증이 존재하는 경우 응답코드 409을 반환한다.")
    void 인증_생성_요청_시_중복된_인증이_존재하는_경우_응답코드_409을_반환한다 () throws Exception {
        /* given */
        ProofRequest.Create request = 인증_생성_요청;

        when(proofService.save(any(User.class), any(ProofRequest.Create.class)))
                .thenThrow(new DuplicatedProofException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "save" + invalidConflictPath + "duplicated",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("에러 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("인증 생성 요청 시 인증 라운드가 챌린지의 인증 횟수를 초과하는 경우 응답코드 400을 반환한다.")
    void 인증_생성_요청_시_인증_라운드가_챌린지의_인증_횟수를_초과하는_경우_응답코드_400을_반환한다 () throws Exception {
        /* given */
        ProofRequest.Create request = 인증_생성_요청;

        when(proofService.save(any(User.class), any(ProofRequest.Create.class)))
                .thenThrow(new ProofCountExceededException(ChallengeFixtures.챌린지_인증_횟수));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.status").value(400),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "save" + invalidConflictPath + "round",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("에러 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 인증 단일 조회 요청 시 응답코드 200을 반환한다.")
    void 정상적인_인증_단일_조회_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long proofId = 인증_ID;

        when(proofService.find(any(User.class), eq(proofId)))
                .thenReturn(인증_요약_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{proofId}", proofId));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(인증_요약_응답.id()),
                jsonPath("$.date").value(인증_요약_응답.date().toString()),
                jsonPath("$.round").value(인증_요약_응답.round())
        );

        result.andDo(document(docsPath + "find",
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("proofId").description("인증 ID")
                ),
                responseFields(
                        fieldWithPath("id").description("인증 ID"),
                        fieldWithPath("date").description("인증 수행 날짜"),
                        fieldWithPath("round").description("인증 라운드")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 인증에 대한 단일 조회 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_인증에_대한_단일_조회_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long proofId = 인증_ID;

        when(proofService.find(any(User.class), eq(proofId)))
                .thenThrow(new NotFoundProofException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{proofId}", proofId));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "find" + invalidNotFoundPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("에러 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("인증 단일 조회 요청 시 수락된 참여자가 아닌 경우 응답코드 403을 반환한다.")
    void 인증_단일_조회_요청_시_수락된_참여자가_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long proofId = 인증_ID;

        when(proofService.find(any(User.class), eq(proofId)))
                .thenThrow(new InvalidParticipationStatusActionException("참여자만 인증을 조회할 수 있습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{proofId}", proofId));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "find" + invalidForbiddenPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("에러 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 인증 목록 조회 요청 시 응답코드 200을 반환한다.")
    void 정상적인_인증_목록_조회_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        PageResponse<ProofResponse.Summary> response = 인증_페이지_응답;

        when(proofService.findAll(any(User.class), eq(challengeId), any()))
                .thenReturn(response);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/challenge/{challengeId}", challengeId)
                        .param("page", "0").param("size", "10"));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.content").isArray(),
                jsonPath("$.content.size()").value(response.content().size()),
                jsonPath("$.totalPages").value(response.totalPages()),
                jsonPath("$.totalElements").value(response.totalElements()),
                jsonPath("$.size").value(response.size()),
                jsonPath("$.number").value(response.number()),
                jsonPath("$.last").value(response.last())
        );

        result.andDo(document(docsPath + "findAll",
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("challengeId").description("챌린지 ID")
                ),
                queryParameters(
                        parameterWithName("page").description("페이지 번호"),
                        parameterWithName("size").description("페이지 크기")
                ),
                responseFields(
                        fieldWithPath("content").description("인증 목록"),
                        fieldWithPath("content[].id").description("인증 ID"),
                        fieldWithPath("content[].date").description("인증 수행 날짜"),
                        fieldWithPath("content[].round").description("인증 라운드"),
                        fieldWithPath("totalPages").description("총 페이지 수"),
                        fieldWithPath("totalElements").description("총 요소 수"),
                        fieldWithPath("size").description("페이지 크기"),
                        fieldWithPath("number").description("현재 페이지 번호"),
                        fieldWithPath("last").description("마지막 페이지 여부")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 챌린지에 대한 인증 목록 조회 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지에_대한_인증_목록_조회_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(proofService.findAll(any(User.class), eq(challengeId), any()))
                .thenThrow(new NotFoundChallengeException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/challenge/{challengeId}", challengeId)
                        .param("page", "0").param("size", "10"));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "findAll" + invalidNotFoundPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("에러 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("인증 목록 조회 요청 시 수락된 참여자가 아닌 경우 응답코드 403을 반환한다.")
    void 인증_목록_조회_요청_시_수락된_참여자가_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(proofService.findAll(any(User.class), eq(challengeId), any()))
                .thenThrow(new InvalidParticipationStatusActionException("참여자만 인증 목록을 조회할 수 있습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/challenge/{challengeId}", challengeId)
                        .param("page", "0").param("size", "10"));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "findAll" + invalidForbiddenPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("에러 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 인증 삭제 요청 시 응답코드 200을 반환한다.")
    void 정상적인_인증_삭제_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long proofId = 인증_ID;

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{proofId}", proofId));

        result.andExpect(status().isOk());
        verify(proofService, atLeastOnce()).delete(any(User.class), eq(proofId));

        result.andDo(document(docsPath + "delete",
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName("proofId").description("인증 ID"))
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 인증에 대한 삭제 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_인증에_대한_삭제_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long proofId = 인증_ID;

        doThrow(new NotFoundProofException())
                .when(proofService).delete(any(User.class), eq(proofId));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{proofId}", proofId));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "delete" + invalidNotFoundPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("에러 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("인증 삭제 요청 시 리더가 아닌 경우 응답코드 403을 반환한다.")
    void 인증_삭제_요청_시_리더가_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long proofId = 인증_ID;

        doThrow(new InvalidChallengeRoleActionException("리더만 인증을 삭제할 수 있습니다."))
                .when(proofService).delete(any(User.class), eq(proofId));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{proofId}", proofId));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "delete" + invalidForbiddenPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("에러 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("인증 삭제 요청 시 삭제 시간 제한을 초과한 경우 응답코드 400을 반환한다.")
    void 인증_삭제_요청_시_삭제_시간_제한을_초과한_경우_응답코드_400을_반환한다 () throws Exception {
        /* given */
        Long proofId = 인증_ID;
        Long hoursBefore = 1L;

        doThrow(new InvalidProofDeletionException("인증 삭제는 시작하기 " + hoursBefore + "시간 이내에만 가능합니다."))
                .when(proofService).delete(any(User.class), eq(proofId));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{proofId}", proofId));

        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.status").value(400),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "delete" + invalidBadRequestPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("에러 데이터")
                )
        ));
    }
}