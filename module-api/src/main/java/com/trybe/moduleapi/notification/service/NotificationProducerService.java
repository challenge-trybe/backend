package com.trybe.moduleapi.notification.service;

import com.trybe.moduleapi.notification.dto.NotificationMessage;
import com.trybe.modulecore.notification.entity.Notification;
import com.trybe.modulecore.notification.enums.NotificationType;
import com.trybe.modulecore.user.entity.User;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationProducerService {
    private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;
    private final NotificationService notificationService;

    public NotificationProducerService(KafkaTemplate<String, NotificationMessage> kafkaTemplate, NotificationService notificationService) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationService = notificationService;
    }

    @Transactional
    public void publishNotification(
            String topic,
            User receiver,
            NotificationType type,
            Long typeId,
            String title,
            String message
    ) {
        Notification notification = new Notification(receiver.getId(), type, typeId, title, message);
        NotificationMessage notificationMessage = NotificationMessage.from(notification, receiver.getUuid());
        
        kafkaTemplate.send(topic, notificationMessage);
        notificationService.save(notification);
    }

    @Transactional
    public void publishNotifications(
            String topic,
            List<User> receivers,
            NotificationType type,
            Long typeId,
            String title,
            String message
    ) {
        List<Notification> notifications = new ArrayList<>();

        for (User receiver : receivers) {
            Notification notification = new Notification(receiver.getId(), type, typeId, title, message);
            notifications.add(notification);

            NotificationMessage notificationMessage = NotificationMessage.from(notification, receiver.getUuid());
            kafkaTemplate.send(topic, notificationMessage);
        }

        notificationService.saveAll(notifications);
    }
}