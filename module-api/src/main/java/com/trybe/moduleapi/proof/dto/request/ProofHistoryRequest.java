package com.trybe.moduleapi.proof.dto.request;

import com.trybe.modulecore.proof.entity.Proof;
import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ProofHistoryRequest {
    private static final String CONTENT_NOT_BLANK_MESSAGE = "인증 내용을 입력해주세요.";
    private static final String CONTENT_MAX_LENGTH_MESSAGE = "인증 내용은 최대 1,000자까지 입력 가능합니다.";

    public record Create(
            @NotBlank(message = CONTENT_NOT_BLANK_MESSAGE)
            @Size(max = 1000, message = CONTENT_MAX_LENGTH_MESSAGE)
            String content
    ) {
        public ProofHistory toEntity(Proof proof, User user, String content) {
            return new ProofHistory(proof, user, content);
        }
    }

    public record Update(
            @NotBlank(message = CONTENT_NOT_BLANK_MESSAGE)
            @Size(max = 1000, message = CONTENT_MAX_LENGTH_MESSAGE)
            String content,
            List<Integer> fileOrder
    ) {}
}
