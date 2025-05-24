package com.trybe.moduleapi.notification.service;

import com.trybe.moduleapi.notification.dto.NotificationMessage;
import com.trybe.modulecore.notification.entity.Notification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationProducerService {
    private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;
    private final NotificationService notificationService;

    private static final String POST_COMMENT_NOTIFICATION_TOPIC = "PostComment_Notification";

    public NotificationProducerService(KafkaTemplate<String, NotificationMessage> kafkaTemplate, NotificationService notificationService) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationService = notificationService;
    }

    @Transactional
    public void publishPostCommentNotification(UUID uuid, Notification notification){
        notificationService.save(notification);
        NotificationMessage notificationMessage = NotificationMessage.from(notification, uuid);

        kafkaTemplate.send(POST_COMMENT_NOTIFICATION_TOPIC, notificationMessage);
    }
}
