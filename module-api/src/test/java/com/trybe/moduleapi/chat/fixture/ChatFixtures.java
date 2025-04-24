package com.trybe.moduleapi.chat.fixture;

import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.chat.dto.ChatRequest;
import com.trybe.moduleapi.chat.dto.ChatResponse;
import com.trybe.moduleapi.common.dto.CursorResponse;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.chat.entity.ChatMessage;
import com.trybe.modulecore.chat.entity.ChatRoom;
import com.trybe.modulecore.chat.enums.MessageType;
import com.trybe.modulecore.user.entity.User;

import java.util.List;

public class ChatFixtures {
    public static final ChatRoom 채팅방(Challenge challenge){
        return new ChatRoom(challenge);
    }
    public static final ChatRoom 채팅방(){
        return new ChatRoom(ChallengeFixtures.챌린지());
    }
    public static final ChatRequest.Send 채팅_메시지_전송_요청 = new ChatRequest.Send("채팅 메시지 전송");

    public static final int 메시지_조회_제한_개수 = 20;
    public static final Long 커서_ID = 6L;

    public static final MessageType 입장 = MessageType.ENTER;
    public static final MessageType 대화 = MessageType.TALK;
    public static final MessageType 퇴장 = MessageType.EXIT;
    public static final MessageType 시스템 = MessageType.SYSTEM;

    public static final ChatMessage 채팅_메시지(ChatRoom chatroom, User sender, String message, MessageType messageType) {
        return ChatMessage.builder()
                .chatRoom(chatroom)
                .user(sender)
                .messageType(messageType)
                .message(message)
                .build();
    }
    public static final ChatMessage 채팅_메시지(MessageType messageType) {
        return ChatMessage.builder()
                .chatRoom(채팅방())
                .user(UserFixtures.회원)
                .messageType(messageType)
                .message("채팅 메시지")
                .build();
    }

    public static final List<ChatMessage> 채팅_메시지_내역 = List.of(채팅_메시지(시스템),채팅_메시지(입장),채팅_메시지(퇴장),채팅_메시지(대화),채팅_메시지(대화), 채팅_메시지(대화));
    public static final CursorResponse<ChatResponse.Message> 채팅_메시지_내역_응답 = CursorResponse.of(채팅_메시지_내역.stream().map(ChatResponse.Message::from).toList(), null, 채팅_메시지_내역.size(), false);
}
