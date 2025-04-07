package com.trybe.moduleapi.config;

import com.trybe.moduleapi.auth.websocket.WebSocketAuthenticationHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private static final String ENDPOINT = "/ws-connection";
    private static final String SIMPLE_BROKER = "/sub";
    private static final String PUBLISH = "/pub";

    private final WebSocketAuthenticationHandler stompSecurityHandler;

    public WebSocketConfig(WebSocketAuthenticationHandler stompSecurityHandler) {
        this.stompSecurityHandler = stompSecurityHandler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(ENDPOINT)
                .setAllowedOriginPatterns("*")
                .withSockJS(); // sock.js를 통하여 낮은 버전의 브라우저에서도 websocket이 동작할수 있게 설정

        registry.addEndpoint(ENDPOINT)
                .setAllowedOriginPatterns("*"); // api 통신 시, withSockJS() 설정을 빼야됨
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(SIMPLE_BROKER);
        registry.setApplicationDestinationPrefixes(PUBLISH);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompSecurityHandler);
    }
}
