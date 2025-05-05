package com.trybe.moduleapi.challenge.controller;

import com.trybe.moduleapi.annotation.WithCustomMockUser;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.challenge.service.ChallengeBookmarkService;
import com.trybe.moduleapi.common.ControllerTest;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.trybe.moduleapi.challenge.fixtures.ChallengeBookmarkFixtures.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChallengeBookmarkController.class)
class ChallengeBookmarkControllerTest extends ControllerTest {
    @MockitoBean
    private ChallengeBookmarkService challengeBookmarkService;

    private final String endpoint = "/api/v1/challenges/bookmarks";

    private final String docsPath = "challenge-bookmark-controller-test/";

    @Test
    @WithCustomMockUser
    @DisplayName("챌린지 북마크 추가 요청 시 응답코드 200을 반환한다.")
    void 챌린지_북마크_추가_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeBookmarkService.addBookmark(any(User.class), eq(challengeId)))
                .thenReturn(북마크_참_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{challengeId}", challengeId));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.bookmarkCount").value(북마크_참_응답.bookmarkCount()),
                jsonPath("$.bookmarked").value(북마크_참_응답.bookmarked())
        );

        result.andDo(document(docsPath + "add",
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("challengeId").description("북마크 추가할 챌린지 ID")
                ),
                responseFields(
                        fieldWithPath("bookmarkCount").description("챌린지의 북마크 수"),
                        fieldWithPath("bookmarked").description("북마크 여부")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 챌린지에 대해 북마크 추가 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지에_대해_북마크_추가_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;

        when(challengeBookmarkService.addBookmark(any(User.class), eq(challengeId)))
                .thenThrow(new NotFoundChallengeException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/{challengeId}", challengeId));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "add" + invalidNotFoundPath,
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
    @DisplayName("챌린지 북마크 삭제 요청 시 응답코드 200을 반환한다.")
    void 챌린지_북마크_삭제_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeBookmarkService.removeBookmark(any(User.class), eq(challengeId)))
                .thenReturn(북마크_거짓_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{challengeId}", challengeId));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.bookmarkCount").value(북마크_거짓_응답.bookmarkCount()),
                jsonPath("$.bookmarked").value(북마크_거짓_응답.bookmarked())
        );

        result.andDo(document(docsPath + "remove",
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("challengeId").description("북마크 삭제할 챌린지 ID")
                ),
                responseFields(
                        fieldWithPath("bookmarkCount").description("챌린지의 북마크 수"),
                        fieldWithPath("bookmarked").description("북마크 여부")
                )
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 챌린지에 대해 북마크 삭제 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_챌린지에_대해_북마크_삭제_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;

        when(challengeBookmarkService.removeBookmark(any(User.class), eq(challengeId)))
                .thenThrow(new NotFoundChallengeException());

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{challengeId}", challengeId));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "remove" + invalidNotFoundPath,
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
    @DisplayName("나의 북마크 챌린지 목록 조회 요청 시 응답코드 200을 반환한다.")
    void 나의_북마크_챌린지_목록_조회_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        PageResponse<ChallengeResponse.Preview> response = ChallengeFixtures.챌린지_미리보기_페이지_응답;

        when(challengeBookmarkService.getMyBookmarkedChallenges(any(User.class), any()))
                .thenReturn(response);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/my"));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.content").isArray(),
                jsonPath("$.content[0].id").value(response.content().get(0).id()),
                jsonPath("$.content[0].thumbnail.originalName").value(response.content().get(0).thumbnail().originalName()),
                jsonPath("$.content[0].thumbnail.filePath").value(response.content().get(0).thumbnail().filePath()),
                jsonPath("$.content[0].title").value(response.content().get(0).title()),
                jsonPath("$.content[0].description").value(response.content().get(0).description()),
                jsonPath("$.content[0].status").value(response.content().get(0).status().name()),
                jsonPath("$.content[0].category").value(response.content().get(0).category().name()),
                jsonPath("$.content[0].capacity").value(response.content().get(0).capacity()),
                jsonPath("$.content[0].participantCount").value(response.content().get(0).participantCount()),
                jsonPath("$.content[0].bookmark.bookmarkCount").value(response.content().get(0).bookmark().bookmarkCount()),
                jsonPath("$.content[0].bookmark.bookmarked").value(response.content().get(0).bookmark().bookmarked()),
                jsonPath("$.totalPages").value(response.totalPages()),
                jsonPath("$.totalElements").value(response.totalElements()),
                jsonPath("$.size").value(response.size()),
                jsonPath("$.number").value(response.number()),
                jsonPath("$.last").value(response.last())
        );

        result.andDo(document(docsPath + "my",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("content").description("챌린지 목록"),
                        fieldWithPath("content[].id").description("챌린지 ID"),
                        fieldWithPath("content[].thumbnail").description("챌린지 썸네일 정보"),
                        fieldWithPath("content[].thumbnail.originalName").description("썸네일 파일 원본 이름"),
                        fieldWithPath("content[].thumbnail.filePath").description("썸네일 파일 경로"),
                        fieldWithPath("content[].title").description("챌린지 제목"),
                        fieldWithPath("content[].description").description("챌린지 설명"),
                        fieldWithPath("content[].status").description("챌린지 상태"),
                        fieldWithPath("content[].category").description("챌린지 카테고리"),
                        fieldWithPath("content[].capacity").description("챌린지 인원"),
                        fieldWithPath("content[].participantCount").description("참여자 수"),
                        fieldWithPath("content[].bookmark.bookmarkCount").description("북마크 수"),
                        fieldWithPath("content[].bookmark.bookmarked").description("북마크 여부"),
                        fieldWithPath("totalPages").description("전체 페이지 수"),
                        fieldWithPath("totalElements").description("전체 요소 수"),
                        fieldWithPath("size").description("페이지 크기"),
                        fieldWithPath("number").description("현재 페이지 번호"),
                        fieldWithPath("last").description("마지막 페이지 여부")
                )
        ));
    }
}