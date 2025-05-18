package com.trybe.moduleapi.notification.service;

import com.trybe.moduleapi.notification.dto.NotificationMessage;
import com.trybe.modulecore.notification.entity.Notification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationProducerService {
    private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;
    private final NotificationService notificationService;

    private static final String CHAT_NOTIFICATION_TOPIC = "Chat_Notification";

    public NotificationProducerService(KafkaTemplate<String, NotificationMessage> kafkaTemplate, NotificationService notificationService) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationService = notificationService;
    }

    @Transactional
    public void publishChatNotification(Map<UUID, Notification> notificationMap) {
        List<Notification> notifications = notificationMap.values().stream().toList();
        notificationService.saveAll(notifications);

        for (Map.Entry<UUID, Notification> entry  : notificationMap.entrySet()) {
            UUID uuid = entry.getKey();
            Notification notification = entry.getValue();
            NotificationMessage notificationMessage = toNotificationMessage(uuid, notification);
            kafkaTemplate.send(CHAT_NOTIFICATION_TOPIC, notificationMessage);
        }
    }

    private NotificationMessage toNotificationMessage(UUID uuid, Notification notification){
        return NotificationMessage.from(notification, uuid);
    }
}
