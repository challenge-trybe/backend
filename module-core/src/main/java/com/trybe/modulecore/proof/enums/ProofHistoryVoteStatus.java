package com.trybe.modulecore.proof.enums;

import lombok.Getter;

@Getter
public enum ProofHistoryVoteStatus {
    APPROVED("approved"),
    DISAPPROVED("disapproved");

    private final String value;
    ProofHistoryVoteStatus(String value) {
        this.value = value;
    }

    public static ProofHistoryVoteStatus fromBoolean(boolean approved) {
        return approved ? APPROVED : DISAPPROVED;
    }

    public static ProofHistoryVoteStatus fromValue(String value) {
        return switch (value) {
            case "approved" -> APPROVED;
            case "disapproved" -> DISAPPROVED;
            default -> null;
        };
    }

    public boolean toBoolean() {
        return this == APPROVED;
    }
}
