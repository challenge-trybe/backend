package com.trybe.moduleapi.notification.service;

import com.trybe.moduleapi.notification.dto.NotificationMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducerService {
    private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;
    private final NotificationService notificationService;

    public NotificationProducerService(KafkaTemplate<String, NotificationMessage> kafkaTemplate, NotificationService notificationService) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationService = notificationService;
    }
}
