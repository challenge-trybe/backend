package com.trybe.modulecore.proof.entity;

import com.trybe.modulecore.common.entity.BaseEntity;
import com.trybe.modulecore.proof.enums.ProofHistoryStatus;
import com.trybe.modulecore.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "proof_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE proof_histories SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ProofHistory extends BaseEntity {
    public ProofHistory(Proof proof, User user, String content) {
        this.proof = proof;
        this.user = user;
        this.content = content;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proof_id", nullable = false, updatable = false)
    private Proof proof;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "status", nullable = false)
    private ProofHistoryStatus status = ProofHistoryStatus.PENDING;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateStatus(ProofHistoryStatus status) {
        this.status = status;
    }
}