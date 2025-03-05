package com.trybe.modulecore.proof.entity;

import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "proof")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE proof SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Proof extends BaseEntity {
    public Proof(Challenge challenge, LocalDate date, int round) {
        this.challenge = challenge;
        this.date = date;
        this.round = round;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false, updatable = false)
    private Challenge challenge;

    @Column(name = "date", nullable = false, updatable = false)
    private LocalDate date;

    @Column(name = "round", nullable = false, updatable = false)
    private int round;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}