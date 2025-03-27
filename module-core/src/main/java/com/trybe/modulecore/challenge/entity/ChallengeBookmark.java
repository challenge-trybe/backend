package com.trybe.modulecore.challenge.entity;

import com.trybe.modulecore.common.entity.BaseEntity;
import com.trybe.modulecore.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "challenge_bookmarks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeBookmark extends BaseEntity {
    public ChallengeBookmark(Challenge challenge, User user) {
        this.challenge = challenge;
        this.user = user;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false, updatable = false)
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;
}