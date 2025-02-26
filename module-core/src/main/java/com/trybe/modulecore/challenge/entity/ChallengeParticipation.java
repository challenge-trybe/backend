package com.trybe.modulecore.challenge.entity;

import com.trybe.modulecore.challenge.enums.ChallengeRole;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.common.entity.BaseEntity;
import com.trybe.modulecore.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenge_participation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE challenge_participation SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ChallengeParticipation extends BaseEntity {
    public ChallengeParticipation(User user, Challenge challenge, ChallengeRole role, ParticipationStatus status) {
        this.user = user;
        this.challenge = challenge;
        this.role = role;
        this.status = status;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false, updatable = false)
    private Challenge challenge;

    @Column(name = "role", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private ChallengeRole role;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ParticipationStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void updateStatus(ParticipationStatus status) {
        this.status = status;
    }
}