package com.trybe.modulecore.chat.entity;

import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_rooms")
@SQLDelete(sql = "UPDATE chat_rooms SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ChatRoom extends BaseEntity {
    public ChatRoom(Challenge challenge) {
        this.challenge = challenge;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @OneToOne
    @JoinColumn(name = "challenge_id", nullable = false, updatable = false)
    private Challenge challenge;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
