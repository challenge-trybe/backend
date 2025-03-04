package com.trybe.moduleapi.challenge.controller;

import com.trybe.moduleapi.annotation.WithCustomMockUser;
import com.trybe.moduleapi.challenge.dto.ChallengeParticipationRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeParticipationResponse;
import com.trybe.moduleapi.challenge.exception.InvalidChallengeStatusException;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.challenge.exception.participation.*;
import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.challenge.service.ChallengeParticipationService;
import com.trybe.moduleapi.common.ControllerTest;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;

import static com.trybe.moduleapi.challenge.fixtures.ChallengeParticipationFixtures.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChallengeParticipationController.class)
class ChallengeParticipationControllerTest extends ControllerTest {
    @MockitoBean
    private ChallengeParticipationService challengeParticipationService;

    private final String endpoint = "/api/v1/challenges/participations";

    private final String docsPath = "challenge-participation-controller-test/";
    private final String invalidBadRequestPath = "/invalid/bad-request/";
    private final String invalidNotFoundPath = "/invalid/not-found/";
    private final String invalidConflictPath = "/invalid/conflict/";
    private final String invalidForbiddenPath = "/invalid/forbidden/";

    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 챌린지 참여 신청 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_참여_신청_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeParticipationResponse.Detail response = 챌린지_참여_상세_응답();

        when(challengeParticipationService.join(any(User.class), any(Long.class)))
                .thenReturn(response);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{challengeId}", challengeId));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.challenge.id").value(response.challenge().id()),
                jsonPath("$.challenge.title").value(response.challenge().title()),
                jsonPath("$.challenge.description").value(response.challenge().description()),
                jsonPath("$.challenge.status").value(response.challenge().status().toString()),
                jsonPath("$.challenge.capacity").value(response.challenge().capacity()),
                jsonPath("$.challenge.category").value(response.challenge().category().toString()),
                jsonPath("$.id").value(response.id()),
                jsonPath("$.user.id").value(response.user().id()),
                jsonPath("$.user.nickname").value(response.user().nickname()),
                jsonPath("$.user.userId").value(response.user().userId()),
                jsonPath("$.user.email").value(response.user().email()),
                jsonPath("$.user.gender").value(response.user().gender().toString()),
                jsonPath("$.user.birth").value(response.user().birth().toString()),
                jsonPath("$.role").value(response.role().toString()),
                jsonPath("$.status").value(response.status().toString())
//                jsonPath("$.createdAt").value(response.createdAt().toString())
        );

        result.andDo(document(docsPath + "join",
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName("challengeId").description("챌린지 ID")),
                responseFields(
                        fieldWithPath("challenge.id").description("챌린지 ID"),
                        fieldWithPath("challenge.title").description("챌린지 제목"),
                        fieldWithPath("challenge.description").description("챌린지 설명"),
                        fieldWithPath("challenge.status").description("챌린지 상태"),
                        fieldWithPath("challenge.capacity").description("챌린지 인원 수"),
                        fieldWithPath("challenge.category").description("챌린지 카테고리"),
                        fieldWithPath("id").description("참여 ID"),
                        fieldWithPath("user.id").description("유저 ID"),
                        fieldWithPath("user.nickname").description("유저 닉네임"),
                        fieldWithPath("user.userId").description("유저 아이디"),
                        fieldWithPath("user.email").description("유저 이메일"),
                        fieldWithPath("user.gender").description("유저 성별"),
                        fieldWithPath("user.birth").description("유저 생년월일"),
                        fieldWithPath("role").description("참여자 역할"),
                        fieldWithPath("status").description("참여 상태"),
                        fieldWithPath("createdAt").description("참여 생성일자")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 챌린지에 대한 참여 신청 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지에_대한_참여_신청_요청_시_응답코드_404을_반환하다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;

        when(challengeParticipationService.join(any(User.class), any(Long.class)))
                .thenThrow(new NotFoundChallengeException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{challengeId}", challengeId));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "join" + invalidNotFoundPath,
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName("challengeId").description("챌린지 ID")),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("챌린지 참여 신청 요청 시 이미 챌린지 참여 정보가 존재할 경우 응답코드 409을 반환한다.")
    void 챌린지_참여_신청_요청_시_이미_챌린지_참여_정보가_존재할_경우_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeParticipationService.join(any(User.class), any(Long.class)))
                .thenThrow(new DuplicatedChallengeParticipationException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{challengeId}", challengeId));

        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "join" + invalidConflictPath + "duplicated",
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
    @DisplayName("챌린지 참여 신청 요청 시 진행 예정 상태의 챌린지가 아닌 경우 응답코드 409을 반환한다.")
    void 챌린지_참여_신청_요청_시_진행_예정_상태의_챌린지가_아닌_경우_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeParticipationService.join(any(User.class), any(Long.class)))
                .thenThrow(new InvalidChallengeStatusException("챌린지가 진행 예정인 경우에만 참여 신청이 가능합니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{challengeId}", challengeId));

        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "join" + invalidConflictPath + "challenge-status",
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
    @DisplayName("챌린지 참여 신청 요청 시 챌린지 참여 인원이 꽉 찬 경우 응답코드 409을 반환한다.")
    void 챌린지_참여_신청_요청_시_챌린지_참여_인원이_꽉_찬_경우_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeParticipationService.join(any(User.class), any(Long.class)))
                .thenThrow(new ChallengeFullException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{challengeId}", challengeId));

        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "join" + invalidConflictPath + "challenge-full",
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
    @DisplayName("챌린지 참여 신청 요청 시 챌린지 참여 신청 대기열이 꽉 찬 경우 응답코드 409을 반환한다.")
    void 챌린지_참여_신청_요청_시_챌린지_참여_신청_대기열이_꽉_찬_경우_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeParticipationService.join(any(User.class), any(Long.class)))
                .thenThrow(new ChallengeParticipationFullException("참여 신청이 꽉 찼습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{challengeId}", challengeId));

        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "join" + invalidConflictPath + "challenge-participation-full",
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
    @DisplayName("정상적인 나의 챌린지 참여 목록 조회 요청 시 응답코드 200을 반환한다.")
    void 정상적인_나의_챌린지_참여_목록_조회_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        PageResponse<ChallengeParticipationResponse.Detail> response = 나의_참여_중인_챌린지_목록_페이지_응답;

        when(challengeParticipationService.getMyParticipations(any(User.class), any(ParticipationStatus.class), any()))
                .thenReturn(나의_참여_중인_챌린지_목록_페이지_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/my")
                .param("page", "0").param("size", "10")
                .param("status", 챌린지_참여_수락_상태.toString()));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.content[0].challenge.id").value(response.content().get(0).challenge().id()),
                jsonPath("$.content[0].challenge.title").value(response.content().get(0).challenge().title()),
                jsonPath("$.content[0].challenge.description").value(response.content().get(0).challenge().description()),
                jsonPath("$.content[0].challenge.status").value(response.content().get(0).challenge().status().toString()),
                jsonPath("$.content[0].challenge.capacity").value(response.content().get(0).challenge().capacity()),
                jsonPath("$.content[0].challenge.category").value(response.content().get(0).challenge().category().toString()),
                jsonPath("$.content[0].id").value(response.content().get(0).id()),
                jsonPath("$.content[0].user.id").value(response.content().get(0).user().id()),
                jsonPath("$.content[0].user.nickname").value(response.content().get(0).user().nickname()),
                jsonPath("$.content[0].user.userId").value(response.content().get(0).user().userId()),
                jsonPath("$.content[0].user.email").value(response.content().get(0).user().email()),
                jsonPath("$.content[0].user.gender").value(response.content().get(0).user().gender().toString()),
                jsonPath("$.content[0].user.birth").value(response.content().get(0).user().birth().toString()),
                jsonPath("$.content[0].role").value(response.content().get(0).role().toString()),
                jsonPath("$.content[0].status").value(response.content().get(0).status().toString()),
//                jsonPath("$.content[0].createdAt").value(response.content().get(0).createdAt().toString())
                jsonPath("$.totalPages").value(response.totalPages()),
                jsonPath("$.totalElements").value(response.totalElements()),
                jsonPath("$.size").value(response.size()),
                jsonPath("$.number").value(response.number()),
                jsonPath("$.last").value(response.last())
        );

        result.andDo(document(docsPath + "my-participations",
                preprocessResponse(prettyPrint()),
                queryParameters(
                        parameterWithName("status").description("참여 상태"),
                        parameterWithName("page").description("페이지 번호"),
                        parameterWithName("size").description("페이지 크기")
                ),
                responseFields(
                        fieldWithPath("content[0].challenge.id").description("챌린지 ID"),
                        fieldWithPath("content[0].challenge.title").description("챌린지 제목"),
                        fieldWithPath("content[0].challenge.description").description("챌린지 설명"),
                        fieldWithPath("content[0].challenge.status").description("챌린지 상태"),
                        fieldWithPath("content[0].challenge.capacity").description("챌린지 인원 수"),
                        fieldWithPath("content[0].challenge.category").description("챌린지 카테고리"),
                        fieldWithPath("content[0].id").description("참여 ID"),
                        fieldWithPath("content[0].user.id").description("유저 ID"),
                        fieldWithPath("content[0].user.nickname").description("유저 닉네임"),
                        fieldWithPath("content[0].user.userId").description("유저 아이디"),
                        fieldWithPath("content[0].user.email").description("유저 이메일"),
                        fieldWithPath("content[0].user.gender").description("유저 성별"),
                        fieldWithPath("content[0].user.birth").description("유저 생년월일"),
                        fieldWithPath("content[0].role").description("참여자 역할"),
                        fieldWithPath("content[0].status").description("참여 상태"),
                        fieldWithPath("content[0].createdAt").description("참여 생성일자"),
                        fieldWithPath("totalPages").description("전체 페이지 수"),
                        fieldWithPath("totalElements").description("전체 요소 수"),
                        fieldWithPath("size").description("페이지 크기"),
                        fieldWithPath("number").description("현재 페이지 번호"),
                        fieldWithPath("last").description("마지막 페이지 여부")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 챌린지 참여자 목록 조회 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_참여자_목록_조회_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        PageResponse<ChallengeParticipationResponse.Summary> response = 챌린지_참여_목록_페이지_응답;

        when(challengeParticipationService.getParticipants(any(User.class), any(Long.class), any(ParticipationStatus.class), any()))
                .thenReturn(챌린지_참여_목록_페이지_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{challengeId}", challengeId)
                .param("page", "0").param("size", "10")
                .param("status", 챌린지_참여_수락_상태.toString()));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.content[0].id").value(response.content().get(0).id()),
                jsonPath("$.content[0].user.id").value(response.content().get(0).user().id()),
                jsonPath("$.content[0].user.nickname").value(response.content().get(0).user().nickname()),
                jsonPath("$.content[0].user.userId").value(response.content().get(0).user().userId()),
                jsonPath("$.content[0].user.email").value(response.content().get(0).user().email()),
                jsonPath("$.content[0].user.gender").value(response.content().get(0).user().gender().toString()),
                jsonPath("$.content[0].user.birth").value(response.content().get(0).user().birth().toString()),
                jsonPath("$.content[0].role").value(response.content().get(0).role().toString()),
                jsonPath("$.content[0].status").value(response.content().get(0).status().toString()),
//                jsonPath("$.content[0].createdAt").value(response.content().get(0).createdAt().toString())
                jsonPath("$.totalPages").value(response.totalPages()),
                jsonPath("$.totalElements").value(response.totalElements()),
                jsonPath("$.size").value(response.size()),
                jsonPath("$.number").value(response.number()),
                jsonPath("$.last").value(response.last())
        );

        result.andDo(document(docsPath + "participants",
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName("challengeId").description("챌린지 ID")),
                queryParameters(
                        parameterWithName("status").description("참여 상태"),
                        parameterWithName("page").description("페이지 번호"),
                        parameterWithName("size").description("페이지 크기")
                ),
                responseFields(
                        fieldWithPath("content[0].id").description("참여 ID"),
                        fieldWithPath("content[0].user.id").description("유저 ID"),
                        fieldWithPath("content[0].user.nickname").description("유저 닉네임"),
                        fieldWithPath("content[0].user.userId").description("유저 아이디"),
                        fieldWithPath("content[0].user.email").description("유저 이메일"),
                        fieldWithPath("content[0].user.gender").description("유저 성별"),
                        fieldWithPath("content[0].user.birth").description("유저 생년월일"),
                        fieldWithPath("content[0].role").description("참여자 역할"),
                        fieldWithPath("content[0].status").description("참여 상태"),
                        fieldWithPath("content[0].createdAt").description("참여 생성일자"),
                        fieldWithPath("totalPages").description("전체 페이지 수"),
                        fieldWithPath("totalElements").description("전체 요소 수"),
                        fieldWithPath("size").description("페이지 크기"),
                        fieldWithPath("number").description("현재 페이지 번호"),
                        fieldWithPath("last").description("마지막 페이지 여부")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 챌린지에 대한 참여자 목록 조회 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지에_대한_참여자_목록_조회_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;

        when(challengeParticipationService.getParticipants(any(User.class), any(Long.class), any(ParticipationStatus.class), any()))
                .thenThrow(new NotFoundChallengeException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{challengeId}", challengeId)
                .param("page", "0").param("size", "10")
                .param("status", 챌린지_참여_수락_상태.toString()));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "participants" + invalidNotFoundPath,
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
    @DisplayName("챌린지 참여자 목록 조회 요청 시 요청자가 수락된 참여자가 아니라면 응답코드 403을 반환한다.")
    void 챌린지_참여자_목록_조회_요청_시_요청자가_수락된_참여자가_아니라면_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeParticipationService.getParticipants(any(User.class), any(Long.class), any(ParticipationStatus.class), any()))
                .thenThrow(new InvalidParticipationStatusActionException("참여 상태가 수락됨인 참여자만 접근 가능합니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{challengeId}", challengeId)
                .param("page", "0").param("size", "10")
                .param("status", 챌린지_참여_수락_상태.toString()));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "participants" + invalidConflictPath + "participation-status",
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
    @DisplayName("챌린지 참여자 목록 조회 요청 시 리더가 아닌 요청자가 수락된 참여자 외의 조회를 수행하면 응답코드 403을 반환한다.")
    void 챌린지_참여자_목록_조회_요청_시_리더가_아닌_요청자가_수락된_참여자_외의_조회를_수행하면_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeParticipationService.getParticipants(any(User.class), any(Long.class), any(ParticipationStatus.class), any()))
                .thenThrow(new InvalidChallengeRoleActionException("리더만 참여 신청 목록을 조회할 수 있습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/{challengeId}", challengeId)
                .param("page", "0").param("size", "10")
                .param("status", 챌린지_참여_수락_상태.toString()));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "participants" + invalidConflictPath + "challenge-role",
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
    @DisplayName("정상적인 챌린지 참여 처리 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_참여_처리_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long participationId = 챌린지_참여_ID;
        ChallengeParticipationRequest.Confirm request = 챌린지_참여_처리_요청;
        ChallengeParticipationResponse.Detail response = 챌린지_수락된_참여_상세_응답();

        when(challengeParticipationService.confirm(any(User.class), any(Long.class), any(ParticipationStatus.class)))
                .thenReturn(response);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/confirm/{participationId}", participationId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.challenge.id").value(response.challenge().id()),
                jsonPath("$.challenge.title").value(response.challenge().title()),
                jsonPath("$.challenge.description").value(response.challenge().description()),
                jsonPath("$.challenge.status").value(response.challenge().status().toString()),
                jsonPath("$.challenge.capacity").value(response.challenge().capacity()),
                jsonPath("$.challenge.category").value(response.challenge().category().toString()),
                jsonPath("$.id").value(response.id()),
                jsonPath("$.user.id").value(response.user().id()),
                jsonPath("$.user.nickname").value(response.user().nickname()),
                jsonPath("$.user.userId").value(response.user().userId()),
                jsonPath("$.user.email").value(response.user().email()),
                jsonPath("$.user.gender").value(response.user().gender().toString()),
                jsonPath("$.user.birth").value(response.user().birth().toString()),
                jsonPath("$.role").value(response.role().toString()),
                jsonPath("$.status").value(response.status().toString()),
                jsonPath("$.createdAt").value(response.createdAt().toString())
        );

        result.andDo(document(docsPath + "confirm",
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName("participationId").description("참여 ID")),
                requestFields(fieldWithPath("status").description("처리할 참여 상태 (수락 혹은 거절)")),
                responseFields(
                        fieldWithPath("challenge.id").description("챌린지 ID"),
                        fieldWithPath("challenge.title").description("챌린지 제목"),
                        fieldWithPath("challenge.description").description("챌린지 설명"),
                        fieldWithPath("challenge.status").description("챌린지 상태"),
                        fieldWithPath("challenge.capacity").description("챌린지 인원 수"),
                        fieldWithPath("challenge.category").description("챌린지 카테고리"),
                        fieldWithPath("id").description("참여 ID"),
                        fieldWithPath("user.id").description("유저 ID"),
                        fieldWithPath("user.nickname").description("유저 닉네임"),
                        fieldWithPath("user.userId").description("유저 아이디"),
                        fieldWithPath("user.email").description("유저 이메일"),
                        fieldWithPath("user.gender").description("유저 성별"),
                        fieldWithPath("user.birth").description("유저 생년월일"),
                        fieldWithPath("role").description("참여자 역할"),
                        fieldWithPath("status").description("참여 상태"),
                        fieldWithPath("createdAt").description("참여 생성일자")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 챌린지 참여에 대한 참여 처리 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지_참여에_대한_참여_처리_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long participationId = 챌린지_참여_ID;
        ChallengeParticipationRequest.Confirm request = 챌린지_참여_처리_요청;

        when(challengeParticipationService.confirm(any(User.class), any(Long.class), any(ParticipationStatus.class)))
                .thenThrow(new NotFoundChallengeParticipationException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/confirm/{participationId}", participationId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "confirm" + invalidNotFoundPath,
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
    @DisplayName("챌린지 참여 처리 요청 시 챌린지에 대한 참여자가 아니라면 응답코드 404을 반환한다.")
    void 챌린지_참여_처리_요청_시_챌린지에_대한_참여자가_아니라면_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long participationId = 챌린지_참여_ID;
        ChallengeParticipationRequest.Confirm request = 챌린지_참여_처리_요청;

        when(challengeParticipationService.confirm(any(User.class), any(Long.class), any(ParticipationStatus.class)))
                .thenThrow(new NotFoundChallengeParticipationException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/confirm/{participationId}", participationId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "confirm" + invalidNotFoundPath + "participant",
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
    @DisplayName("챌린지 참여 처리 요청 시 요청자가 리더가 아니라면 응답코드 403을 반환한다.")
    void 챌린지_참여_처리_요청_시_요청자가_리더가_아니라면_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long participationId = 챌린지_참여_ID;
        ChallengeParticipationRequest.Confirm request = 챌린지_참여_처리_요청;

        when(challengeParticipationService.confirm(any(User.class), any(Long.class), any(ParticipationStatus.class)))
                .thenThrow(new InvalidChallengeRoleActionException("리더만 참여자를 처리할 수 있습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/confirm/{participationId}", participationId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "confirm" + invalidForbiddenPath + "challenge-role",
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
    @DisplayName("챌린지 참여 처리 요청 시 챌린지가 진행 예정 상태가 아닌 경우 응답코드 409을 반환한다.")
    void 챌린지_참여_처리_요청_시_챌린지가_진행_예정_상태가_아닌_경우_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long participationId = 챌린지_참여_ID;
        ChallengeParticipationRequest.Confirm request = 챌린지_참여_처리_요청;

        when(challengeParticipationService.confirm(any(User.class), any(Long.class), any(ParticipationStatus.class)))
                .thenThrow(new InvalidChallengeStatusException("챌린지가 진행 예정인 경우에만 참여 신청을 처리할 수 있습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/confirm/{participationId}", participationId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "confirm" + invalidConflictPath + "challenge-status",
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
    @DisplayName("챌린지 참여 처리 요청 시 챌린지 참여 인원이 꽉 찬 경우 응답코드 409을 반환한다.")
    void 챌린지_참여_처리_요청_시_챌린지_참여_인원이_꽉_찬_경우_응답코드_409을_반환한다 () throws Exception {
        /* given */
        Long participationId = 챌린지_참여_ID;
        ChallengeParticipationRequest.Confirm request = 챌린지_참여_처리_요청;

        when(challengeParticipationService.confirm(any(User.class), any(Long.class), any(ParticipationStatus.class)))
                .thenThrow(new ChallengeFullException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/confirm/{participationId}", participationId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isConflict(),
                jsonPath("$.status").value(409),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "confirm" + invalidConflictPath + "challenge-full",
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
    @DisplayName("챌린지 참여 처리 요청 시 처리하려는 챌린지 참여 상태가 대기 중이 아닌 경우 응답코드 400을 반환한다.")
    void 챌린지_참여_처리_요청_시_처리하려는_챌린지_참여_상태가_대기_중이_아닌_경우_응답코드_400을_반환한다 () throws Exception {
        /* given */
        Long participationId = 챌린지_참여_ID;
        ChallengeParticipationRequest.Confirm request = 챌린지_참여_처리_요청;

        when(challengeParticipationService.confirm(any(User.class), any(Long.class), any(ParticipationStatus.class)))
                .thenThrow(new InvalidParticipationStatusException("참여 상태가 대기 중인 참여자만 처리할 수 있습니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/confirm/{participationId}", participationId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.status").value(400),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "confirm" + invalidBadRequestPath + "participation-status",
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
    @DisplayName("챌린지 참여 처리 요청 시 처리 상태가 수락 혹은 거절이 아닌 경우 응답코드 400을 반환한다.")
    void 챌린지_참여_처리_요청_시_처리_상태가_수락_혹은_거절이_아닌_경우_응답코드_400을_반환한다 () throws Exception {
        /* given */
        Long participationId = 챌린지_참여_ID;
        ChallengeParticipationRequest.Confirm request = 잘못된_챌린지_참여_처리_요청;

        when(challengeParticipationService.confirm(any(User.class), any(Long.class), any(ParticipationStatus.class)))
                .thenThrow(new InvalidParticipationStatusException("참여 수락 또는 거절만 가능합니다."));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/confirm/{participationId}", participationId)
                .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpectAll(
                status().isBadRequest(),
                jsonPath("$.status").value(400),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "confirm" + invalidBadRequestPath + "request-participation-status",
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
    @DisplayName("정상적인 챌린지 탈퇴 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_탈퇴_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/leave/{challengeId}", challengeId));

        result.andExpect(status().isOk());
        verify(challengeParticipationService, atLeastOnce()).leave(any(User.class), any(Long.class));

        result.andDo(document(docsPath + "leave",
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName("challengeId").description("챌린지 ID"))
        ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("챌린지 탈퇴 요청 시 챌린지 참여 정보가 존재하지 않는 경우 응답코드 404을 반환한다.")
    void 챌린지_탈퇴_요청_시_챌린지_참여_정보가_존재하지_않는_경우_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        doThrow(new NotFoundChallengeParticipationException())
                .when(challengeParticipationService).leave(any(User.class), any(Long.class));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/leave/{challengeId}", challengeId));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "leave" + invalidNotFoundPath,
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
    @DisplayName("챌린지 탈퇴 요청 시 요청자가 리더인 경우 응답코드 403을 반환한다.")
    void 챌린지_탈퇴_요청_시_요청자가_리더인_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        doThrow(new InvalidChallengeRoleActionException("리더는 챌린지를 탈퇴할 수 없습니다."))
                .when(challengeParticipationService).leave(any(User.class), any(Long.class));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/leave/{challengeId}", challengeId));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "leave" + invalidForbiddenPath + "challenge-role",
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
    @DisplayName("정상적인 챌린지 참여 신청 취소 요청 시 응답코드 200을 반환한다.")
    void 정상적인_챌린지_참여_신청_취소_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long participationId = 챌린지_참여_ID;

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{participationId}", participationId));

        result.andExpect(status().isOk());
        verify(challengeParticipationService, atLeastOnce()).cancel(any(User.class), any(Long.class));

        result.andDo(document(docsPath + "cancel",
                preprocessResponse(prettyPrint()),
                pathParameters(parameterWithName("participationId").description("참여 ID"))
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("챌린지 참여 신청 취소 요청 시 챌린지 참여가 존재하지 않는 경우 응답코드 404을 반환한다.")
    void 챌린지_참여_신청_취소_요청_시_챌린지_참여가_존재하지_않는_경우_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long participationId = 챌린지_참여_ID;

        doThrow(new NotFoundChallengeParticipationException())
                .when(challengeParticipationService).cancel(any(User.class), any(Long.class));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{participationId}", participationId));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "cancel" + invalidNotFoundPath,
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
    @DisplayName("챌린지 참여 신청 취소 요청 시 요청자가 참여 신청자가 아닌 경우 응답코드 403을 반환한다.")
    void 챌린지_참여_신청_취소_요청_시_요청자가_참여_신청자가_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long participationId = 챌린지_참여_ID;

        doThrow(new ForbiddenParticipationException("해당 챌린지 참여 정보에 접근할 수 없습니다."))
                .when(challengeParticipationService).cancel(any(User.class), any(Long.class));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{participationId}", participationId));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "cancel" + invalidForbiddenPath,
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
    @DisplayName("챌린지 참여 신청 취소 요청 시 챌린지 참여 상태가 대기 중이 아닌 경우 응답코드 403을 반환한다.")
    void 챌린지_참여_신청_취소_요청_시_챌린지_참여_상태가_대기_중이_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long participationId = 챌린지_참여_ID;

        doThrow(new InvalidParticipationStatusActionException("참여 상태가 대기중인 참여자만 접근 가능합니다."))
                .when(challengeParticipationService).cancel(any(User.class), any(Long.class));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{participationId}", participationId));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").exists(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "cancel" + invalidForbiddenPath + "participation-status",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("status").description("응답 상태 코드"),
                        fieldWithPath("message").description("응답 메시지"),
                        fieldWithPath("data").description("추가 데이터")
                )
        ));
    }
}