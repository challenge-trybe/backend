package com.trybe.moduleapi.chat.controller;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.chat.dto.ChatRequest;
import com.trybe.moduleapi.chat.dto.ChatResponse;
import com.trybe.moduleapi.chat.service.ChatService;
import com.trybe.moduleapi.common.dto.CursorResponse;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat/{chatRoomId}/send")
    public void send(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @DestinationVariable Long chatRoomId,
            @Payload ChatRequest.Send request) {
        chatService.sendMessage(chatRoomId, userDetails.getUser(), request);
    }

    @GetMapping("/{chatRoomId}")
    public CursorResponse<ChatResponse.Message> findAll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("chatRoomId") Long chatRoomId,
            @RequestParam(value = "cursor", required = false) Long cursorId
    ){
        return chatService.findMessages(userDetails.getUser(), chatRoomId, cursorId);
    }

}
