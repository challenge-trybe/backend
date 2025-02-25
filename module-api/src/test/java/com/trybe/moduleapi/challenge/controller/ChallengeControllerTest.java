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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
class ChallengeControllerTest {
    @InjectMocks
    private ChallengeController challengeController;

    @Mock
    private ChallengeService challengeService;

    @Mock
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private String endpoint = "/api/v1/challenges";

    private final Long TMP_USER_ID = 1L;

    @BeforeEach
    public void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mockMvc = MockMvcBuilders.standaloneSetup(challengeController)
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
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
        mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(request.title()))
                .andExpect(jsonPath("$.description").value(request.description()))
                .andExpect(jsonPath("$.startDate").value(request.startDate().toString().toString()))
                .andExpect(jsonPath("$.endDate").value(request.endDate().toString().toString()))
                .andExpect(jsonPath("$.status").value(ChallengeFixtures.대기중.toString()))
                .andExpect(jsonPath("$.capacity").value(request.capacity()))
                .andExpect(jsonPath("$.category").value(request.category().toString().toString()))
                .andExpect(jsonPath("$.proofWay").value(request.proofWay()))
                .andExpect(jsonPath("$.proofCount").value(request.proofCount()));
    }

    @Test
    @DisplayName("비정상적인 챌린지 생성 요청 시 응답코드 400을 반환한다.")
    void 비정상적인_챌린지_생성_요청_시_응답코드_400을_반환한다 () throws Exception {
        /* given */
        ChallengeRequest.Create request = ChallengeFixtures.잘못된_챌린지_생성_요청;

        /* when */
        /* then */
        mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(jsonPath("$.data.title").exists())
                .andExpect(jsonPath("$.data.description").exists())
                .andExpect(jsonPath("$.data.datesAfterToday").exists())
                .andExpect(jsonPath("$.data.capacity").exists())
                .andExpect(jsonPath("$.data.proofWay").exists())
                .andExpect(jsonPath("$.data.proofCount").exists());
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
        mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/" + challengeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(ChallengeFixtures.챌린지_상세_응답.title()))
                .andExpect(jsonPath("$.description").value(ChallengeFixtures.챌린지_상세_응답.description()))
                .andExpect(jsonPath("$.startDate").value(ChallengeFixtures.챌린지_상세_응답.startDate().toString()))
                .andExpect(jsonPath("$.endDate").value(ChallengeFixtures.챌린지_상세_응답.endDate().toString()))
                .andExpect(jsonPath("$.status").value(ChallengeFixtures.대기중.toString()))
                .andExpect(jsonPath("$.capacity").value(ChallengeFixtures.챌린지_상세_응답.capacity()))
                .andExpect(jsonPath("$.category").value(ChallengeFixtures.챌린지_상세_응답.category().toString()))
                .andExpect(jsonPath("$.proofWay").value(ChallengeFixtures.챌린지_상세_응답.proofWay()))
                .andExpect(jsonPath("$.proofCount").value(ChallengeFixtures.챌린지_상세_응답.proofCount()));
    }

    @Test
    @DisplayName("존재하지 않는 챌린지 단일 조회 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지_단일_조회_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;

        doThrow(new NotFoundChallengeException()).when(challengeService).find(challengeId);

        /* when */
        /* then */
        mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/" + challengeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.data").doesNotExist());
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
        mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/search")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()")
                        .value(ChallengeFixtures.챌린지_목록_응답.size()));
    }

    @Test
    @DisplayName("비정상적인 챌린지 필터링 조회 요청 시 응답코드 400을 반환한다.")
    void 비정상적인_챌린지_필터링_조회_요청_시_응답코드_400을_반환한다 () throws Exception {
        /* given */
        ChallengeRequest.Read request = ChallengeFixtures.잘못된_챌린지_조회_요청;

        /* when */
        /* then */
        mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/search")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(jsonPath("$.data.statuses").exists())
                .andExpect(jsonPath("$.data.categories").exists());
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
        mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/" + challengeId + "/content")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.title()))
                .andExpect(jsonPath("$.description").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.description()))
                .andExpect(jsonPath("$.startDate").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.startDate().toString().toString()))
                .andExpect(jsonPath("$.endDate").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.endDate().toString()))
                .andExpect(jsonPath("$.status").value(ChallengeFixtures.대기중.toString()))
                .andExpect(jsonPath("$.capacity").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.capacity()))
                .andExpect(jsonPath("$.category").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.category().toString()))
                .andExpect(jsonPath("$.proofWay").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.proofWay()))
                .andExpect(jsonPath("$.proofCount").value(ChallengeFixtures.내용_수정된_챌린지_상세_응답.proofCount()));
    }

    @Test
    @DisplayName("비정상적인 챌린지 내용 수정 요청 시 응답코드 400을 반환한다.")
    void 비정상적인_챌린지_내용_수정_요청_시_응답코드_400을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateContent request = ChallengeFixtures.잘못된_챌린지_내용_수정_요청;

        /* when */
        /* then */
        mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/" + challengeId + "/content")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(jsonPath("$.data.title").exists())
                .andExpect(jsonPath("$.data.description").exists())
                .andExpect(jsonPath("$.data.datesAfterToday").exists())
                .andExpect(jsonPath("$.data.capacity").exists());
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
        mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/" + challengeId + "/content")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.data").doesNotExist());
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
        mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/" + challengeId + "/proof")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.title()))
                .andExpect(jsonPath("$.description").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.description()))
                .andExpect(jsonPath("$.startDate").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.startDate().toString()))
                .andExpect(jsonPath("$.endDate").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.endDate().toString()))
                .andExpect(jsonPath("$.status").value(ChallengeFixtures.대기중.toString()))
                .andExpect(jsonPath("$.capacity").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.capacity()))
                .andExpect(jsonPath("$.category").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.category().toString()))
                .andExpect(jsonPath("$.proofWay").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.proofWay()))
                .andExpect(jsonPath("$.proofCount").value(ChallengeFixtures.인증_내용_수정된_챌린지_상세_응답.proofCount()));
    }

    @Test
    @DisplayName("비정상적인 챌린지 인증 내용 수정 요청 시 응답코드 400을 반환한다.")
    void 비정상적인_챌린지_인증_내용_수정_요청_시_응답코드_400을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateProof request = ChallengeFixtures.잘못된_챌린지_인증_내용_수정_요청;

        /* when */
        /* then */
        mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/" + challengeId + "/proof")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(jsonPath("$.data.proofWay").exists())
                .andExpect(jsonPath("$.data.proofCount").exists());
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
        mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/" + challengeId + "/proof")
                .contentType(MediaType.APPLICATION_JSON).characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("챌린지 삭제 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_삭제_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        /* when */
        /* then */
        mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/" + challengeId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 챌린지 삭제 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지_삭제_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;

        doThrow(new NotFoundChallengeException()).when(challengeService).delete(challengeId, TMP_USER_ID);

        /* when */
        /* then */
        mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/" + challengeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}