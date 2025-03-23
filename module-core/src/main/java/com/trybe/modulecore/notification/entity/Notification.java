package com.trybe.modulecore.notification.entity;

import com.trybe.modulecore.common.entity.BaseEntity;
import com.trybe.modulecore.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {
    public Notification(Long userId, NotificationType type, Long typeId, String title, String message) {
        this.userId = userId;
        this.type = type;
        this.typeId = typeId;
        this.title = title;
        this.message = message;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private NotificationType type;

    @Column(name = "type_id", nullable = false, updatable = false)
    private Long typeId;

    @Column(name = "title", nullable = false, updatable = false)
    private String title;

    @Column(name = "message", nullable = false, updatable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    public void markAsRead() {
        this.isRead = true;
    }
}