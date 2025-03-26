package com.trybe.moduleapi.proof.controller;

import com.trybe.moduleapi.annotation.WithCustomMockUser;
import com.trybe.moduleapi.challenge.exception.participation.InvalidParticipationStatusActionException;
import com.trybe.moduleapi.common.ControllerTest;
import com.trybe.moduleapi.proof.exception.history.DuplicatedProofHistoryVoteException;
import com.trybe.moduleapi.proof.exception.history.ForbiddenProofHistoryException;
import com.trybe.moduleapi.proof.exception.history.InvalidProofHistoryStatusException;
import com.trybe.moduleapi.proof.exception.history.NotFoundProofHistoryException;
import com.trybe.moduleapi.proof.fixtures.ProofHistoryFixtures;
import com.trybe.moduleapi.proof.service.ProofHistoryVoteService;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.trybe.moduleapi.proof.fixtures.ProofHistoryVoteFixtures.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProofHistoryVoteController.class)
public class ProofHistoryVoteControllerTest extends ControllerTest {
    @MockitoBean
    private ProofHistoryVoteService proofHistoryVoteService;

    private final String endpoint = "/api/v1/proofs/histories/votes";

    private final String docsPath = "proof-history-vote-controller-test/";
    private final String invalidNotFoundPath = "/invalid/not-found/";
    private final String invalidConflictPath = "/invalid/conflict/";
    private final String invalidForbiddenPath = "/invalid/forbidden/";

    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 인증 기록 투표 생성 요청 시 응답코드 200을 반환한다.")
    void 정상적인_인증_기록_투표_생성_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = ProofHistoryFixtures.인증_기록_ID;
        boolean approved = 찬성_여부;

        when(proofHistoryVoteService.save(any(User.class), eq(proofHistoryId), eq(approved)))
            .thenReturn(나의_투표_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{proofHistoryId}?approved={approved}", proofHistoryId, approved));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.approved").value(approved)
        );

        result.andDo(document(docsPath + "save",
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("proofHistoryId").description("인증 기록 ID")
                ),
                queryParameters(
                        parameterWithName("approved").description("찬성 여부 (true: 찬성, false: 반대)")
                ),
                responseFields(
                        fieldWithPath("approved").description("찬성 여부")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 인증 기록에 대한 투표 생성 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_인증_기록에_대한_투표_생성_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = ProofHistoryFixtures.인증_기록_ID;
        boolean approved = 찬성_여부;

        when(proofHistoryVoteService.save(any(User.class), eq(proofHistoryId), eq(approved)))
            .thenThrow(new NotFoundProofHistoryException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{proofHistoryId}", proofHistoryId)
                .param("approved", String.valueOf(approved)));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "save" + invalidNotFoundPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("인증 기록 투표 생성 요청 시 챌린지 멤버가 아닌 경우 응답코드 403을 반환한다.")
    void 인증_기록_투표_생성_요청_시_챌린지_멤버가_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = ProofHistoryFixtures.인증_기록_ID;
        boolean approved = 찬성_여부;

        when(proofHistoryVoteService.save(any(User.class), eq(proofHistoryId), eq(approved)))
            .thenThrow(new InvalidParticipationStatusActionException("챌린지 멤버만 인증 기록에 대해 투표할 수 있습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{proofHistoryId}", proofHistoryId)
                .param("approved", String.valueOf(approved)));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "save" + invalidForbiddenPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("인증 기록 투표 생성 요청 시 자신의 인증 기록에 대한 투표인 경우 응답코드 403을 반환한다.")
    void 인증_기록_투표_생성_요청_시_자신의_인증_기록에_대한_투표인_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = ProofHistoryFixtures.인증_기록_ID;
        boolean approved = 찬성_여부;

        when(proofHistoryVoteService.save(any(User.class), eq(proofHistoryId), eq(approved)))
            .thenThrow(new ForbiddenProofHistoryException("자기 자신의 인증 기록에 투표할 수 없습니다."));
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{proofHistoryId}", proofHistoryId)
                .param("approved", String.valueOf(approved)));
        
        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );
        
        result.andDo(document(docsPath + "save" + invalidForbiddenPath + "self",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("인증 기록 투표 생성 요청 시 이미 처리된 인증 기록일 경우 응답코드 409을 반환한다.")
    void 인증_기록_투표_생성_요청_시_이미_처리된_인증_기록일_경우_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = ProofHistoryFixtures.인증_기록_ID;
        boolean approved = 찬성_여부;

        when(proofHistoryVoteService.save(any(User.class), eq(proofHistoryId), eq(approved)))
            .thenThrow(new InvalidProofHistoryStatusException("이미 처리된 인증 기록에 대해 투표할 수 없습니다."));
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{proofHistoryId}", proofHistoryId)
                .param("approved", String.valueOf(approved)));
        
        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );
        
        result.andDo(document(docsPath + "save" + invalidConflictPath + "status",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("인증 기록 투표 생성 요청 시 이미 투표한 경우 응답코드 409을 반환한다.")
    void 인증_기록_투표_생성_요청_시_이미_투표한_경우_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = ProofHistoryFixtures.인증_기록_ID;
        boolean approved = 찬성_여부;

        when(proofHistoryVoteService.save(any(User.class), eq(proofHistoryId), eq(approved)))
            .thenThrow(new DuplicatedProofHistoryVoteException());
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{proofHistoryId}", proofHistoryId)
                .param("approved", String.valueOf(approved)));
        
        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );
        
        result.andDo(document(docsPath + "save" + invalidConflictPath + "duplicated",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 인증 기록 투표 이력 조회 요청 시 응답코드 200을 반환한다.")
    void 정상적인_인증_기록_투표_이력_조회_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = ProofHistoryFixtures.인증_기록_ID;

        when(proofHistoryVoteService.findMyVote(any(User.class), eq(proofHistoryId)))
            .thenReturn(나의_투표_응답);
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{proofHistoryId}", proofHistoryId));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.approved").value(찬성_여부)
        );

        result.andDo(document(docsPath + "find-my-vote",
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("proofHistoryId").description("인증 기록 ID")
                ),
                responseFields(
                        fieldWithPath("approved").description("찬성 여부")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 인증 기록에 대한 투표 이력 조회 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_인증_기록에_대한_투표_이력_조회_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = ProofHistoryFixtures.인증_기록_ID;

        when(proofHistoryVoteService.findMyVote(any(User.class), eq(proofHistoryId)))
            .thenThrow(new NotFoundProofHistoryException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{proofHistoryId}", proofHistoryId));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "find-my-vote" + invalidNotFoundPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("인증 기록 투표 이력 조회 시 챌린지 멤버가 아닌 경우 응답코드 403을 반환한다.")
    void 인증_기록_투표_이력_조회_시_챌린지_멤버가_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = ProofHistoryFixtures.인증_기록_ID;

        when(proofHistoryVoteService.findMyVote(any(User.class), eq(proofHistoryId)))
            .thenThrow(new InvalidParticipationStatusActionException("챌린지 멤버만 투표 내역을 조회할 수 있습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{proofHistoryId}", proofHistoryId));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "find-my-vote" + invalidForbiddenPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("인증 기록 투표 이력 조회 시 이미 처리된 인증 기록일 경우 응답코드 409을 반환한다.")
    void 인증_기록_투표_이력_조회_시_이미_처리된_인증_기록일_경우_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = ProofHistoryFixtures.인증_기록_ID;

        when(proofHistoryVoteService.findMyVote(any(User.class), eq(proofHistoryId)))
            .thenThrow(new InvalidProofHistoryStatusException("이미 처리된 인증 기록에 대한 투표 내역을 조회할 수 없습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{proofHistoryId}", proofHistoryId));

        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "find-my-vote" + invalidConflictPath + "status",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 인증 기록 투표 결과 조회 요청 시 응답코드 200을 반환한다.")
    void 정상적인_인증_기록_투표_결과_조회_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = ProofHistoryFixtures.인증_기록_ID;

        when(proofHistoryVoteService.getResult(any(User.class), eq(proofHistoryId)))
            .thenReturn(투표_결과_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{proofHistoryId}/result", proofHistoryId));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.approvedCount").value(찬성_투표_수),
                jsonPath("$.disapprovedCount").value(반대_투표_수),
                jsonPath("$.nonParticipatedCount").value(투표_미참여_인원_수)
        );

        result.andDo(document(docsPath + "get-result",
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("proofHistoryId").description("인증 기록 ID")
                ),
                responseFields(
                        fieldWithPath("approvedCount").description("찬성 투표 수"),
                        fieldWithPath("disapprovedCount").description("반대 투표 수"),
                        fieldWithPath("nonParticipatedCount").description("투표 미참여 인원 수")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 인증 기록에 대한 투표 결과 조회 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_인증_기록에_대한_투표_결과_조회_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = ProofHistoryFixtures.인증_기록_ID;

        when(proofHistoryVoteService.getResult(any(User.class), eq(proofHistoryId)))
            .thenThrow(new NotFoundProofHistoryException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{proofHistoryId}/result", proofHistoryId));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "get-result" + invalidNotFoundPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("인증 기록 투표 결과 조회 요청 시 인증 기록 작성자가 아닌 경우 응답코드 403을 반환한다.")
    void 인증_기록_투표_결과_조회_요청_시_인증_기록_작성자가_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = ProofHistoryFixtures.인증_기록_ID;

        when(proofHistoryVoteService.getResult(any(User.class), eq(proofHistoryId)))
            .thenThrow(new ForbiddenProofHistoryException("인증 기록의 작성자만 투표 결과를 조회할 수 있습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{proofHistoryId}/result", proofHistoryId));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "get-result" + invalidForbiddenPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("인증 기록 투표 결과 조회 요청 시 이미 처리된 인증 기록인 경우 응답코드 409을 반환한다.")
    void 인증_기록_투표_결과_조회_요청_시_이미_처리된_인증_기록인_경우_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = ProofHistoryFixtures.인증_기록_ID;

        when(proofHistoryVoteService.getResult(any(User.class), eq(proofHistoryId)))
            .thenThrow(new InvalidProofHistoryStatusException("이미 처리된 인증 기록에 대한 투표 결과를 조회할 수 없습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{proofHistoryId}/result", proofHistoryId));

        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "get-result" + invalidConflictPath + "status",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }
}