package com.trybe.modulecore.challenge.entity;

import com.trybe.modulecore.challenge.enums.ChallengeCategory;
import com.trybe.modulecore.challenge.enums.ChallengeStatus;
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
@Table(name = "challenge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE challenge SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Challenge extends BaseEntity {
    public Challenge(String title, String description, LocalDate startDate, LocalDate endDate, int capacity, ChallengeCategory category, String proofWay, int proofCount) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.category = category;
        this.proofWay = proofWay;
        this.proofCount = proofCount;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChallengeStatus status = ChallengeStatus.PENDING;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChallengeCategory category;

    @Column(name = "proof_way", nullable = false)
    private String proofWay;

    @Column(name = "proof_count", nullable = false)
    private int proofCount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void updateContent(String title, String description, LocalDate startDate, LocalDate endDate, int capacity, ChallengeCategory category) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.category = category;
    }

    public void updateProof(String proofWay, int proofCount) {
        this.proofWay = proofWay;
        this.proofCount = proofCount;
    }

    public void updateStatus(ChallengeStatus status) {
        this.status = status;
    }
}
