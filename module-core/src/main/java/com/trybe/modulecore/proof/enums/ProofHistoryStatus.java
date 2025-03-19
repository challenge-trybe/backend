package com.trybe.modulecore.proof.enums;

import lombok.Getter;

@Getter
public enum ProofHistoryStatus {
    PENDING("대기"),
    PASSED("성공"),
    FAILED("실패");

    private final String description;

    ProofHistoryStatus(String description) {
        this.description = description;
    }

    public boolean is(ProofHistoryStatus status) {
        return this == status;
    }

    public boolean isNot(ProofHistoryStatus status) {
        return this != status;
    }
}
