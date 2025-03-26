package com.trybe.moduleapi.proof.controller;

import com.trybe.moduleapi.annotation.WithCustomMockUser;
import com.trybe.moduleapi.challenge.exception.participation.InvalidParticipationStatusActionException;
import com.trybe.moduleapi.common.ControllerTest;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.proof.dto.request.ProofHistoryRequest;
import com.trybe.moduleapi.proof.dto.response.ProofHistoryResponse;
import com.trybe.moduleapi.proof.exception.InvalidProofDateException;
import com.trybe.moduleapi.proof.exception.NotFoundProofException;
import com.trybe.moduleapi.proof.exception.history.DuplicatedProofHistoryException;
import com.trybe.moduleapi.proof.exception.history.ForbiddenProofHistoryException;
import com.trybe.moduleapi.proof.exception.history.InvalidProofHistoryStatusException;
import com.trybe.moduleapi.proof.exception.history.NotFoundProofHistoryException;
import com.trybe.moduleapi.proof.fixtures.ProofFixtures;
import com.trybe.moduleapi.proof.service.ProofHistoryService;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;
import static com.trybe.moduleapi.proof.fixtures.ProofHistoryFixtures.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProofHistoryController.class)
public class ProofHistoryControllerTest extends ControllerTest {
    @MockitoBean
    private ProofHistoryService proofHistoryService;

    private final String endpoint = "/api/v1/proofs/histories";

    private final String docsPath = "proof-history-controller-test/";
    private final String invalidBadRequestPath = "/invalid/bad-request/";
    private final String invalidNotFoundPath = "/invalid/not-found/";
    private final String invalidConflictPath = "/invalid/conflict/";
    private final String invalidForbiddenPath = "/invalid/forbidden/";

    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 인증 기록 생성 요청 시 응답코드 200을 반환한다.")
    void 정상적인_인증_기록_생성_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long proofId = ProofFixtures.인증_ID;
        ProofHistoryRequest.Create request = 인증_기록_생성_요청;

        when(proofHistoryService.save(any(User.class), eq(proofId), eq(request)))
                .thenReturn(대기_인증_기록_요약_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{proofId}", proofId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(대기_인증_기록_요약_응답.id()),
                jsonPath("$.content").value(대기_인증_기록_요약_응답.content()),
                jsonPath("$.status").value(대기_인증_기록_요약_응답.status().toString()),
                jsonPath("$.createdAt").value(대기_인증_기록_요약_응답.createdAt().toString())
        );
        
        result.andDo(document(docsPath + "save",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("proofId").description("인증 ID")
                ),
                requestFields(
                        fieldWithPath("content").description("인증 기록 내용")
                ),
                responseFields(
                        fieldWithPath("id").description("인증 기록 ID"),
                        fieldWithPath("content").description("인증 기록 내용"),
                        fieldWithPath("status").description("인증 기록 상태"),
                        fieldWithPath("createdAt").description("인증 기록 생성 시간")
                )
        ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("비정상적인 인증 기록 생성 요청 시 응답코드 400을 반환한다.")
    void 비정상적인_인증_기록_생성_요청_시_응답코드_400을_반환한다 () throws Exception {
        /* given */
        Long proofId = ProofFixtures.인증_ID;
        ProofHistoryRequest.Create request = 잘못된_인증_기록_생성_요청;
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{proofId}", proofId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));
        
        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.status").value(400),
                jsonPath("$.message").doesNotExist(),
                jsonPath("$.data.content").exists()
        );
        
        result.andDo(document(docsPath + "save" + invalidBadRequestPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data.content").description("인증 기록 내용 에러 메시지")
                )
        ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 인증에 대한 인증 기록 생성 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_인증에_대한_인증_기록_생성_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long proofId = ProofFixtures.인증_ID;
        ProofHistoryRequest.Create request = 인증_기록_생성_요청;
        
        when(proofHistoryService.save(any(User.class), eq(proofId), eq(request)))
                .thenThrow(new NotFoundProofException());
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{proofId}", proofId)
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
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("인증 기록 생성 요청 시 챌린지 멤버가 아닌 경우 응답코드 403을 반환한다.")
    void 인증_기록_생성_요청_시_챌린지_멤버가_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long proofId = ProofFixtures.인증_ID;
        ProofHistoryRequest.Create request = 인증_기록_생성_요청;
        
        when(proofHistoryService.save(any(User.class), eq(proofId), eq(request)))
                .thenThrow(new InvalidParticipationStatusActionException("멤버만 인증 기록을 등록할 수 있습니다."));
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{proofId}", proofId)
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
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("인증 기록 생성 요청 시 인증 날짜가 아닌 경우 응답코드 400을 반환한다.")
    void 인증_기록_생성_요청_시_인증_날짜가_아닌_경우_응답코드_400을_반환한다 () throws Exception {
        /* given */
        Long proofId = ProofFixtures.인증_ID;
        ProofHistoryRequest.Create request = 인증_기록_생성_요청;
        
        when(proofHistoryService.save(any(User.class), eq(proofId), eq(request)))
                .thenThrow(new InvalidProofDateException());
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{proofId}", proofId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));
        
        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.status").value(400),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );
        
        result.andDo(document(docsPath + "save" + invalidBadRequestPath + "date",
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
    @DisplayName("인증 기록 생성 요청 시 인증에 대한 중복된 인증 기록이 있는 경우 응답코드 409을 반환한다.")
    void 인증_기록_생성_요청_시_인증에_대한_중복된_인증_기록이_있는_경우_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long proofId = ProofFixtures.인증_ID;
        ProofHistoryRequest.Create request = 인증_기록_생성_요청;
        
        when(proofHistoryService.save(any(User.class), eq(proofId), eq(request)))
                .thenThrow(new DuplicatedProofHistoryException());
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{proofId}", proofId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));
        
        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );
        
        result.andDo(document(docsPath + "save" + invalidConflictPath,
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
    @DisplayName("정상적인 인증 기록 목록 조회 요청 시 응답코드 200을 반환한다.")
    void 정상적인_인증_기록_목록_조회_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long proofId = ProofFixtures.인증_ID;
        PageResponse<ProofHistoryResponse.Summary> response = 인증_기록_목록_페이지_응답;
        
        when(proofHistoryService.findAll(any(User.class), eq(proofId), any(Pageable.class)))
                .thenReturn(response);
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/all/{proofId}", proofId)
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
                        parameterWithName("proofId").description("인증 ID")
                ),
                queryParameters(
                        parameterWithName("page").description("페이지 번호"),
                        parameterWithName("size").description("페이지 크기")
                ),
                responseFields(
                        fieldWithPath("content").description("인증 기록 요약 목록"),
                        fieldWithPath("content[].id").description("인증 기록 ID"),
                        fieldWithPath("content[].content").description("인증 기록 내용"),
                        fieldWithPath("content[].status").description("인증 기록 상태"),
                        fieldWithPath("content[].createdAt").description("인증 기록 생성 시간"),
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
    @DisplayName("존재하지 않는 인증에 대한 인증 기록 목록 조회 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_인증에_대한_인증_기록_목록_조회_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long proofId = ProofFixtures.인증_ID;
        
        when(proofHistoryService.findAll(any(User.class), eq(proofId), any(Pageable.class)))
                .thenThrow(new NotFoundProofException());
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/all/{proofId}", proofId)
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
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("인증 기록 목록 조회 요청 시 챌린지 멤버가 아닌 경우 응답코드 403을 반환한다.")
    void 인증_기록_목록_조회_요청_시_챌린지_멤버가_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long proofId = ProofFixtures.인증_ID;
        
        when(proofHistoryService.findAll(any(User.class), eq(proofId), any(Pageable.class)))
                .thenThrow(new InvalidParticipationStatusActionException("멤버만 인증 기록을 조회할 수 있습니다."));
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/all/{proofId}", proofId)
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
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 인증 기록 수정 요청 시 응답코드 200을 반환한다.")
    void 정상적인_인증_기록_수정_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        ProofHistoryRequest.Update request =인증_기록_수정_요청;

        when(proofHistoryService.update(any(User.class), eq(proofHistoryId), eq(request)))
                .thenReturn(대기_인증_기록_요약_응답);
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{proofHistoryId}", proofHistoryId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(대기_인증_기록_요약_응답.id()),
                jsonPath("$.content").value(대기_인증_기록_요약_응답.content()),
                jsonPath("$.status").value(대기_인증_기록_요약_응답.status().toString()),
                jsonPath("$.createdAt").value(대기_인증_기록_요약_응답.createdAt().toString())
        );

        result.andDo(document(docsPath + "update",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("proofHistoryId").description("인증 기록 ID")
                ),
                requestFields(
                        fieldWithPath("content").description("인증 기록 내용")
                ),
                responseFields(
                        fieldWithPath("id").description("인증 기록 ID"),
                        fieldWithPath("content").description("인증 기록 내용"),
                        fieldWithPath("status").description("인증 기록 상태"),
                        fieldWithPath("createdAt").description("인증 기록 생성 시간")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("비정상적인 인증 기록 수정 요청 시 응답코드 400을 반환한다.")
    void 비정상적인_인증_기록_수정_요청_시_응답코드_400을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        ProofHistoryRequest.Update request = 잘못된_인증_기록_수정_요청;

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{proofHistoryId}", proofHistoryId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.status").value(400),
                jsonPath("$.message").doesNotExist(),
                jsonPath("$.data.content").exists()
        );

        result.andDo(document(docsPath + "update" + invalidBadRequestPath,
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data.content").description("인증 기록 내용 에러 메시지")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 인증 기록에 대한 인증 기록 수정 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_인증_기록에_대한_인증_기록_수정_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        ProofHistoryRequest.Update request = 인증_기록_수정_요청;

        when(proofHistoryService.update(any(User.class), eq(proofHistoryId), eq(request)))
                .thenThrow(new NotFoundProofHistoryException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{proofHistoryId}", proofHistoryId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "update" + invalidNotFoundPath,
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
    @DisplayName("인증 기록 수정 요청 시 인증 기록의 작성자가 아닌 경우 응답코드 403을 반환한다.")
    void 인증_기록_수정_요청_시_인증_기록의_작성자가_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        ProofHistoryRequest.Update request = 인증_기록_수정_요청;

        when(proofHistoryService.update(any(User.class), eq(proofHistoryId), eq(request)))
                .thenThrow(new ForbiddenProofHistoryException("인증 기록의 작성자만 수정할 수 있습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{proofHistoryId}", proofHistoryId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "update" + invalidForbiddenPath,
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
    @DisplayName("인증 기록 수정 요청 시 이미 처리된 인증 기록인 경우 응답코드 409을 반환한다.")
    void 인증_기록_수정_요청_시_이미_처리된_인증_기록인_경우_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        ProofHistoryRequest.Update request = 인증_기록_수정_요청;

        when(proofHistoryService.update(any(User.class), eq(proofHistoryId), eq(request)))
                .thenThrow(new InvalidProofHistoryStatusException("인증 기록의 작성자만 수정할 수 있습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{proofHistoryId}", proofHistoryId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "update" + invalidConflictPath,
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
    @DisplayName("정상적인 인증 기록 삭제 요청 시 응답코드 200을 반환한다.")
    void 정상적인_인증_기록_삭제_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{proofHistoryId}", proofHistoryId));

        result.andExpect(status().isOk());
        verify(proofHistoryService, atLeastOnce()).delete(any(User.class), eq(proofHistoryId));
        
        result.andDo(document(docsPath + "delete",
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName("proofHistoryId").description("인증 기록 ID"))
        ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 인증 기록에 대한 인증 기록 삭제 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_인증_기록에_대한_인증_기록_삭제_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = 인증_기록_ID;

        doThrow(new NotFoundProofHistoryException())
                .when(proofHistoryService).delete(any(User.class), eq(proofHistoryId));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{proofHistoryId}", proofHistoryId));

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
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("인증 삭제 요청 시 인증 기록의 작성자가 아닌 경우 응답코드 403을 반환한다.")
    void 인증_기록_삭제_요청_시_인증_기록의_작성자가_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long proofHistoryId = 인증_기록_ID;

        doThrow(new ForbiddenProofHistoryException("인증 기록의 작성자만 삭제할 수 있습니다."))
                .when(proofHistoryService).delete(any(User.class), eq(proofHistoryId));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{proofHistoryId}", proofHistoryId));

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
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }
}
