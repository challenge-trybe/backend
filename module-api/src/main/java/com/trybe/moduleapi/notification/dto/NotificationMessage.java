package com.trybe.moduleapi.notification.dto;

import com.trybe.modulecore.notification.entity.Notification;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class NotificationMessage {
    private UUID userUuid;
    private String type;
    private Long typeId;
    private String title;
    private String message;
    private LocalDateTime timestamp;

    public static NotificationMessage from(Notification notification, UUID userUuid) {
        NotificationMessage message = new NotificationMessage();
        message.userUuid = userUuid;
        message.type = notification.getType().toString();
        message.typeId = notification.getTypeId();
        message.title = notification.getTitle();
        message.message = notification.getMessage();
        message.timestamp = notification.getCreatedAt();
        return message;
    }
}
