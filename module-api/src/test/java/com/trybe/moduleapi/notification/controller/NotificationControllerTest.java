package com.trybe.moduleapi.notification.controller;

import com.trybe.moduleapi.annotation.WithCustomMockUser;
import com.trybe.moduleapi.common.ControllerTest;
import com.trybe.moduleapi.notification.exception.ForbiddenNotificationException;
import com.trybe.moduleapi.notification.exception.NotFoundNotificationException;
import com.trybe.moduleapi.notification.service.NotificationService;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.trybe.moduleapi.notification.fixtures.NotificationFixtures.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest extends ControllerTest {
    @MockitoBean
    private NotificationService notificationService;

    private final String endpoint = "/api/v1/notifications";

    private final String docsPath = "notification-controller-test/";

    @Test
    @WithCustomMockUser
    @DisplayName("정상적인 나의 알림 목록 조회 요청 시 응답코드 200을 반환한다.")
    void 정상적인_나의_알림_목록_조회_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        when(notificationService.getNotifications(any(User.class), any(Pageable.class)))
                .thenReturn(알림_페이지_응답);

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/my")
                .param("page", "0").param("size", "10"));

        result.andExpectAll(
                status().isOk(),
                jsonPath("$.content").isArray(),
                jsonPath("$.content[0].id").value(알림_페이지_응답.content().get(0).id()),
                jsonPath("$.content[0].type").value(알림_페이지_응답.content().get(0).type().name()),
                jsonPath("$.content[0].typeId").value(알림_페이지_응답.content().get(0).typeId()),
                jsonPath("$.content[0].title").value(알림_페이지_응답.content().get(0).title()),
                jsonPath("$.content[0].message").value(알림_페이지_응답.content().get(0).message()),
                jsonPath("$.content[0].timestamp").value(알림_페이지_응답.content().get(0).timestamp().toString()),
                jsonPath("$.content[0].isRead").value(알림_페이지_응답.content().get(0).isRead()),
                jsonPath("$.totalElements").value(알림_페이지_응답.totalElements())
        );
        
        result.andDo(document(docsPath + "get-notifications",
                preprocessResponse(prettyPrint()),
                queryParameters(
                        parameterWithName("page").description("페이지 번호"),
                        parameterWithName("size").description("페이지 크기")
                ),
                responseFields(
                        fieldWithPath("content").description("알림 목록"),
                        fieldWithPath("content[].id").description("알림 ID"),
                        fieldWithPath("content[].type").description("알림 타입"),
                        fieldWithPath("content[].typeId").description("알림 타입 ID"),
                        fieldWithPath("content[].title").description("알림 제목"),
                        fieldWithPath("content[].message").description("알림 내용"),
                        fieldWithPath("content[].timestamp").description("알림 생성 시간"),
                        fieldWithPath("content[].isRead").description("알림 읽음 여부"),
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
    @DisplayName("정상적인 알림 읽음 처리 요청 시 응답코드 200을 반환한다.")
    void 정상적인_알림_읽음_처리_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long notificationId = 알림_ID;
        
        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{notificationId}/read", notificationId));
        
        result.andExpect(
                status().isOk()
        );
        
        result.andDo(document(docsPath + "mark-as-read",
                pathParameters(
                        parameterWithName("notificationId").description("알림 ID")
                )
        ));
    }
    
    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 알림에 대한 읽음 처리 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_알림에_대한_읽음_처리_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long notificationId = 알림_ID;

        doThrow(new NotFoundNotificationException()).when(notificationService).markAsRead(any(User.class), any(Long.class));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{notificationId}/read", notificationId));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "mark-as-read" + invalidNotFoundPath,
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
    @DisplayName("알림 읽음 처리 시 자신의 알림이 아닌 경우 응답코드 403을 반환한다.")
    void 알림_읽음_처리_시_자신의_알림이_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long notificationId = 알림_ID;

        doThrow(new ForbiddenNotificationException()).when(notificationService).markAsRead(any(User.class), any(Long.class));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint + "/{notificationId}/read", notificationId));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").isNotEmpty(),
                jsonPath("$.data").doesNotExist()
        );

        result.andDo(document(docsPath + "mark-as-read" + invalidForbiddenPath,
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
    @DisplayName("정상적인 알림 삭제 요청 시 응답코드 200을 반환한다.")
    void 정상적인_알림_삭제_요청_시_응답코드_200을_반환한다 () throws Exception {
        /* given */
        Long notificationId = 알림_ID;

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{notificationId}", notificationId));

        result.andExpect(status().isOk());
        verify(notificationService, times(1)).delete(any(User.class), eq(notificationId));

        result.andDo(document(docsPath + "delete",
                pathParameters(parameterWithName("notificationId").description("알림 ID"))
        ));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("존재하지 않는 알림에 대한 삭제 요청 시 응답코드 404을 반환한다.")
    void 존재하지_않는_알림에_대한_삭제_요청_시_응답코드_404을_반환한다 () throws Exception {
        /* given */
        Long notificationId = 알림_ID;

        doThrow(new NotFoundNotificationException()).when(notificationService).delete(any(User.class), any(Long.class));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{notificationId}", notificationId));

        result.andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value(404),
                jsonPath("$.message").isNotEmpty(),
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
    @DisplayName("알림 삭제 시 자신의 알림이 아닌 경우 응답코드 403을 반환한다.")
    void 알림_삭제_시_자신의_알림이_아닌_경우_응답코드_403을_반환한다 () throws Exception {
        /* given */
        Long notificationId = 알림_ID;

        doThrow(new ForbiddenNotificationException()).when(notificationService).delete(any(User.class), any(Long.class));

        /* when */
        /* then */
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete(endpoint + "/{notificationId}", notificationId));

        result.andExpectAll(
                status().isForbidden(),
                jsonPath("$.status").value(403),
                jsonPath("$.message").isNotEmpty(),
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