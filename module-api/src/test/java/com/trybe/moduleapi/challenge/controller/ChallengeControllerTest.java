package com.trybe.moduleapi.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trybe.moduleapi.challenge.dto.ChallengeRequest;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.challenge.service.ChallengeService;
import com.trybe.moduleapi.common.api.exception.ApiExceptionHandler;
import com.trybe.moduleapi.fixtures.ChallengeFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class})
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
class ChallengeControllerTest {
    @InjectMocks
    private ChallengeController challengeController;

    @Mock
    private ChallengeService challengeService;

    @Mock
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private String endpoint = "/api/v1/challenges";

    private String docsPath = "challenge-controller-test/";
    private final String invalidBadRequestPath = "invalid/bad-request/";
    private final String invalidNotFoundPath = "invalid/not-found/";

    private final Long TMP_USER_ID = 1L;

    @BeforeEach
    public void init(RestDocumentationContextProvider restDocumentation) {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mockMvc = MockMvcBuilders.standaloneSetup(challengeController)
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("정상적인 챌린지 생성 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_생성_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        ChallengeRequest.Create request = ChallengeFixtures.챌린지_생성_요청;

        when(challengeService.save(request, TMP_USER_ID))
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
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/" + challengeId));

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
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/" + challengeId));

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

        when(challengeService.findAll(request))
                .thenReturn(ChallengeFixtures.챌린지_목록_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/search")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.size()").value(ChallengeFixtures.챌린지_목록_응답.size())
        );

        result.andDo(document(docsPath + "search",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                        fieldWithPath("statuses").description("챌린지 상태"),
                        fieldWithPath("categories").description("챌린지 카테고리")
                ),
                responseFields(
                        fieldWithPath("[]").description("챌린지 목록"),
                        fieldWithPath("[].id").description("챌린지 ID"),
                        fieldWithPath("[].title").description("챌린지 제목"),
                        fieldWithPath("[].description").description("챌린지 설명"),
                        fieldWithPath("[].status").description("챌린지 상태"),
                        fieldWithPath("[].capacity").description("챌린지 인원"),
                        fieldWithPath("[].category").description("챌린지 카테고리")
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
    @DisplayName("정상적인 챌린지 내용 수정 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_내용_수정_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateContent request = ChallengeFixtures.챌린지_내용_수정_요청;

        when(challengeService.updateContent(challengeId, request, TMP_USER_ID))
                .thenReturn(ChallengeFixtures.내용_수정된_챌린지_상세_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/" + challengeId + "/content")
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
    @DisplayName("비정상적인 챌린지 내용 수정 요청 시 응답코드 400을 반환한다.")
    void 비정상적인_챌린지_내용_수정_요청_시_응답코드_400을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateContent request = ChallengeFixtures.잘못된_챌린지_내용_수정_요청;

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/" + challengeId + "/content")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.status").value(400),
                jsonPath("$.message").doesNotExist(),
                jsonPath("$.data.title").exists(),
                jsonPath("$.data.description").exists(),
                jsonPath("$.data.datesAfterToday").exists(),
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
                        fieldWithPath("data.capacity").description("챌린지 인원 에러 메시지")
                )));
    }

    @Test
    @DisplayName("존재하지 않는 챌린지 내용 수정 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지_내용_수정_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;
        ChallengeRequest.UpdateContent request = ChallengeFixtures.챌린지_내용_수정_요청;

        doThrow(new NotFoundChallengeException()).when(challengeService).updateContent(challengeId, request, TMP_USER_ID);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/" + challengeId + "/content")
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
    @DisplayName("정상적인 챌린지 인증 내용 수정 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_인증_내용_수정_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateProof request = ChallengeFixtures.챌린지_인증_내용_수정_요청;

        when(challengeService.updateProof(challengeId, request, TMP_USER_ID))
                .thenReturn(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/" + challengeId + "/proof")
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
    @DisplayName("비정상적인 챌린지 인증 내용 수정 요청 시 응답코드 400을 반환한다.")
    void 비정상적인_챌린지_인증_내용_수정_요청_시_응답코드_400을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateProof request = ChallengeFixtures.잘못된_챌린지_인증_내용_수정_요청;

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/" + challengeId + "/proof")
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
    @DisplayName("존재하지 않는 챌린지 인증 내용 수정 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지_인증_내용_수정_요청_시_응답코드_404를_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;
        ChallengeRequest.UpdateProof request = ChallengeFixtures.챌린지_인증_내용_수정_요청;

        doThrow(new NotFoundChallengeException()).when(challengeService).updateProof(challengeId, request, TMP_USER_ID);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/" + challengeId + "/proof")
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
    @DisplayName("챌린지 삭제 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_삭제_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/" + challengeId));

        result.andExpect(status().isOk());

        result.andDo(document(docsPath + "delete",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())));
    }

    @Test
    @DisplayName("존재하지 않는 챌린지 삭제 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지_삭제_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;

        doThrow(new NotFoundChallengeException()).when(challengeService).delete(challengeId, TMP_USER_ID);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/" + challengeId));

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