package com.trybe.moduleapi.notification.dto;

import com.trybe.modulecore.notification.entity.Notification;
import com.trybe.modulecore.notification.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        Long typeId,
        String title,
        String message,
        LocalDateTime timestamp,
        boolean isRead
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTypeId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.isRead()
        );
    }
}
