package com.trybe.modulecore.chat.entity;

import com.trybe.modulecore.chat.enums.MessageType;
import com.trybe.modulecore.common.entity.BaseEntity;
import com.trybe.modulecore.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_messages")
@SQLDelete(sql = "UPDATE chat_messages SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ChatMessage extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatRoom_id", nullable = false, updatable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

    @Column(name = "message", nullable = false, updatable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public ChatMessage(ChatRoom chatRoom, User user, String message, MessageType messageType) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.message = message;
        this.messageType = messageType;
    }
}
