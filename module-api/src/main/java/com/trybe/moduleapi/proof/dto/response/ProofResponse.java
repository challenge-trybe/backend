package com.trybe.moduleapi.proof.dto.response;

import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.modulecore.proof.entity.Proof;

import java.time.LocalDate;

public class ProofResponse {
    public record Summary(
            Long id,
            LocalDate date,
            int round
    ) {
        public static Summary from(Proof proof) {
            return new Summary(
                    proof.getId(),
                    proof.getDate(),
                    proof.getRound()
            );
        }
    }

    public record Detail(
            ChallengeResponse.Summary challenge,
            Long id,
            LocalDate date,
            int round
    ) {
        public static Detail from(Proof proof) {
            return new Detail(
                    ChallengeResponse.Summary.from(proof.getChallenge()),
                    proof.getId(),
                    proof.getDate(),
                    proof.getRound()
            );
        }
    }
}