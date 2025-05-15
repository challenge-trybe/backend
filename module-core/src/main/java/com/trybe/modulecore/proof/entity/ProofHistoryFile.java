package com.trybe.modulecore.proof.entity;

import com.trybe.modulecore.file.entity.File;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "proof_history_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProofHistoryFile {
    public ProofHistoryFile(ProofHistory proofHistory, File file, int fileOrder) {
        this.file = file;
        this.fileOrder = fileOrder;
        proofHistory.addFile(this);
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proof_history_id", nullable = false, updatable = false)
    private ProofHistory proofHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false, updatable = false)
    private File file;

    @Column(name = "file_order", nullable = false)
    private int fileOrder;

    public void updateFileOrder(int order) {
        this.fileOrder = order;
    }
}
