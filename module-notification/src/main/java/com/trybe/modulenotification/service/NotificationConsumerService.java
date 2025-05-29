package com.trybe.modulenotification.service;

import com.trybe.moduleapi.notification.dto.NotificationMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class NotificationConsumerService {
    private final EmitterService emitterService;

    private static final String POST_COMMENT_NOTIFICATION_TOPIC = "PostComment_Notification";
    private static final String CHAT_NOTIFICATION_TOPIC = "Chat_Notification";

    public NotificationConsumerService(EmitterService emitterService) {
        this.emitterService = emitterService;
    }

    @KafkaListener(topics = {
            POST_COMMENT_NOTIFICATION_TOPIC,
            CHAT_NOTIFICATION_TOPIC
    }, groupId = "notification-group")
    public void listen(NotificationMessage message) {
        SseEmitter emitter = emitterService.getEmitter(message.getUserUuid().toString());
        emitterService.sendToClient(message.getUserUuid().toString(), emitter, message);
    }
}
