package com.trybe.modulenotification.service;

import com.trybe.moduleapi.notification.dto.NotificationMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class NotificationConsumerService {
    private final EmitterService emitterService;

    public NotificationConsumerService(EmitterService emitterService) {
        this.emitterService = emitterService;
    }

    // TODO: topics 내에 사용할 topic 작성
    @KafkaListener(topics = {}, groupId = "notification-group")
    public void listen(NotificationMessage message) {
        SseEmitter emitter = emitterService.getEmitter(message.getUserUuid().toString());
        emitterService.sendToClient(message.getUserUuid().toString(), emitter, message);
    }
}