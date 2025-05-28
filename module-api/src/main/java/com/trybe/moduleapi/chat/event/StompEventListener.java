package com.trybe.moduleapi.chat.event;

import com.trybe.moduleapi.chat.exception.WebSocketConnectionException;
import com.trybe.modulecore.chat.repository.ChatRoomUserCache;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Map;

@Component
public class StompEventListener {
    private  final ChatRoomUserCache chatRoomUserCache;

    public StompEventListener(ChatRoomUserCache chatRoomUserCache) {
        this.chatRoomUserCache = chatRoomUserCache;
    }

    @EventListener
    public void handleConnectEvent(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();
        Long chatRoomId = getChatRoomId(accessor);
        String userId = accessor.getUser().getName();

        chatRoomUserCache.online(chatRoomId, userId, sessionId);
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();
        String userId = accessor.getUser().getName();

        chatRoomUserCache.offline(userId, sessionId);
    }

    private Long getChatRoomId(StompHeaderAccessor accessor){
        MessageHeaders messageHeaders = accessor.getMessageHeaders();
        Object simpConnectMessage = messageHeaders.get("simpConnectMessage");

        if (simpConnectMessage instanceof GenericMessage) {
            GenericMessage<?> genericMessage = (GenericMessage<?>) simpConnectMessage;

            Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) genericMessage.getHeaders().get("nativeHeaders");

            if (nativeHeaders != null && nativeHeaders.containsKey("chatRoomId")) {
                List<String> chatRoomIds = nativeHeaders.get("chatRoomId");
                if (!chatRoomIds.isEmpty()) {
                    return Long.valueOf(chatRoomIds.get(0));
                }
            }
        }
        throw new WebSocketConnectionException();
    }
}
