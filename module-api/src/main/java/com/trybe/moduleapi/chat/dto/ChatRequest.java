package com.trybe.moduleapi.chat.dto;

import com.trybe.modulecore.chat.entity.ChatMessage;
import com.trybe.modulecore.chat.entity.ChatRoom;
import com.trybe.modulecore.chat.enums.MessageType;
import com.trybe.modulecore.user.entity.User;

public class ChatRequest {
    public record Send(
            String message
    ){
        public ChatMessage toEntity(ChatRoom chatRoom, User sender){
            return ChatMessage.builder()
                              .chatRoom(chatRoom)
                              .user(sender)
                              .messageType(MessageType.TALK)
                              .message(message)
                              .build();
        }
    }
}
