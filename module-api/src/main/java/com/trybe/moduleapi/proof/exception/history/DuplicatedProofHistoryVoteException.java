package com.trybe.moduleapi.proof.exception.history;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DuplicatedProofHistoryVoteException extends BusinessException {
    public DuplicatedProofHistoryVoteException() {
        super("이미 투표에 참여했습니다.", HttpStatus.CONFLICT.value());
    }
}
