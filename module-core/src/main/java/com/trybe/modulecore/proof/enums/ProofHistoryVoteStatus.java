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

    public static Boolean toBoolean(String value) {
        if (APPROVED.getValue().equals(value)) return true;
        if (DISAPPROVED.getValue().equals(value)) return false;
        return null;
    }
}
