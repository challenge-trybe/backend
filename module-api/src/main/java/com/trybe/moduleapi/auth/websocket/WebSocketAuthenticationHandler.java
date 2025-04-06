package com.trybe.moduleapi.auth.websocket;

import com.trybe.moduleapi.auth.CustomUserDetailsService;
import com.trybe.moduleapi.auth.jwt.JwtUtils;
import com.trybe.moduleapi.auth.websocket.exception.WebSocketAccessDeniedException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthenticationHandler implements ChannelInterceptor {
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService customUserDetailsService;

    public WebSocketAuthenticationHandler(JwtUtils jwtUtils, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtils = jwtUtils;
        this.customUserDetailsService = customUserDetailsService;
    }


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor.getCommand() == StompCommand.CONNECT) {
            String authorization = accessor.getFirstNativeHeader("Authorization");
            String token = jwtUtils.extractToken(authorization);
            if (token == null || !jwtUtils.validateToken(token)) {
                throw new WebSocketAccessDeniedException();
            }
            String userId = jwtUtils.getUserId(token);
            UserDetails userDetails =  customUserDetailsService.loadUserByUsername(userId);
            accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        }
        return message;
    }
}
