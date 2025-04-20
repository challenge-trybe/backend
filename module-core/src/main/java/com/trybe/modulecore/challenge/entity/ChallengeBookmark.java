package com.trybe.modulecore.challenge.entity;

import com.trybe.modulecore.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "challenge_bookmarks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"challenge_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeBookmark extends BaseEntity {
    public ChallengeBookmark(Long challengeId, Long userId) {
        this.challengeId = challengeId;
        this.userId = userId;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "challenge_id", nullable = false, updatable = false)
    private Long challengeId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;
}