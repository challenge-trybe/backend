package com.trybe.moduleapi.chat.controller;

import com.trybe.moduleapi.annotation.WithCustomMockUser;
import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.chat.dto.ChatResponse;
import com.trybe.moduleapi.chat.fixture.ChatFixtures;
import com.trybe.moduleapi.chat.service.ChatService;
import com.trybe.moduleapi.common.ControllerTest;
import com.trybe.moduleapi.common.dto.CursorResponse;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest extends ControllerTest {
    private String docsPath = "chat-controller-test/";

    @MockitoBean
    private ChatService chatService;

    @Test
    @DisplayName("정상적인 채팅 메시지 내역 조회 시 200을 응답한다.")
    @WithCustomMockUser
    void 정상적인_채팅_메시지_내역_조회_시_200을_응답한다() throws Exception {
        /* given */
        Long 커서_ID = ChatFixtures.커서_ID;
        CursorResponse<ChatResponse.Message> 응답 = ChatFixtures.채팅_메시지_내역_응답;

        when(chatService.findMessages(any(User.class), any(Long.class), any(Long.class))).thenReturn(응답);

        /* when */
        mockMvc.perform(get("/api/v1/chats/{challengeId}", ChallengeFixtures.챌린지_ID)
                        .param("cursor", String.valueOf(커서_ID))
                        .contentType(MediaType.APPLICATION_JSON).characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content[0].id").value(응답.content().get(0).id()),
                        jsonPath("$.content[0].sender.id").value(응답.content().get(0).sender().id()),
                        jsonPath("$.content[0].sender.userId").value(응답.content().get(0).sender().userId()),
                        jsonPath("$.content[0].sender.nickname").value(응답.content().get(0).sender().nickname()),
                        jsonPath("$.content[0].message").value(응답.content().get(0).message()),
                        jsonPath("$.content[0].messageType").value(응답.content().get(0).messageType().toString()),
                        jsonPath("$.content[0].createdAt").value(응답.content().get(0).createdAt()),
                        jsonPath("$.nextCursor").value(응답.nextCursor()),
                        jsonPath("$.size").value(응답.size()),
                        jsonPath("$.hasNext").value(응답.hasNext())
                )
                .andDo(document(docsPath,
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("challengeId").description("챌린지 ID")),
                        queryParameters(parameterWithName("cursor").description("커서 ID")),
                        responseFields(
                                fieldWithPath("content[]").description("채팅 메시지 목록"),
                                fieldWithPath("content[].id").description("채팅 메시지 ID"),
                                fieldWithPath("content[].sender").description("메시지 보낸 회원"),
                                fieldWithPath("content[].sender.id").description("메시지 보낸 회원 ID"),
                                fieldWithPath("content[].sender.userId").description("메시지 보낸 회원 userId"),
                                fieldWithPath("content[].sender.nickname").description("메시지 보낸 닉네임"),
                                fieldWithPath("content[].message").description("메시지"),
                                fieldWithPath("content[].messageType").description("메시지 타입"),
                                fieldWithPath("content[].createdAt").description("메시지 생성일"),
                                fieldWithPath("nextCursor").description("다음 커서 ID"),
                                fieldWithPath("size").description("페이지 크기"),
                                fieldWithPath("hasNext").description("다음 페이지 존재 여부")
                        )
                ));

    }
}
