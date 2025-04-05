package com.trybe.moduleapi.chat.dto;

import com.trybe.modulecore.chat.entity.ChatMessage;
import com.trybe.modulecore.chat.entity.ChatRoom;
import com.trybe.modulecore.chat.enums.MessageType;
import com.trybe.modulecore.user.entity.User;

public class ChatRequest {
    public record Send(
            Long challengeId,
            MessageType messageType,
            String message
    ){
        public ChatMessage toEntity(ChatRoom chatRoom, User sendUser, String message){
            return ChatMessage.builder()
                              .chatRoom(chatRoom)
                              .user(sendUser)
                              .messageType(messageType)
                              .message(message)
                              .build();
        }
    }
}
