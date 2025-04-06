package com.trybe.moduleapi.chat.dto;

import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.modulecore.chat.entity.ChatMessage;
import com.trybe.modulecore.chat.enums.MessageType;

import java.time.LocalDateTime;

public class ChatResponse {
    public record Message(
            Long id,
            UserResponse.Summary sendUser,
            String message,
            MessageType messageType,
            LocalDateTime createdAt
    ){
        public static Message from(ChatMessage message){
            UserResponse.Summary summary = null;
            if (message.getUser() != null) {
                summary = UserResponse.Summary.from(message.getUser());
            }
            return new Message(message.getId(), summary, message.getMessage(), message.getMessageType(), message.getCreatedAt());
        }
    }

    public record SystemMessage(
            String message
    ){
        public static SystemMessage from(ChatMessage message){
            return new SystemMessage(message.getMessage());
        }
    }
}
