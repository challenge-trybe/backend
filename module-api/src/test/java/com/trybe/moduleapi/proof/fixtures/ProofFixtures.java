package com.trybe.moduleapi.proof.fixtures;

import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.proof.dto.request.ProofRequest;
import com.trybe.moduleapi.proof.dto.response.ProofResponse;
import com.trybe.modulecore.proof.entity.Proof;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public class ProofFixtures {
    public static final Long 인증_ID = 1L;
    public static final LocalDate 인증_날짜 = LocalDate.of(2025, 4, 14);
    public static final LocalDate 잘못된_인증_날짜 = LocalDate.of(2001, 11, 14);

    public static final int 인증_라운드 = 1;

    /* Request DTO */
    public static final ProofRequest.Create 인증_생성_요청 = new ProofRequest.Create(ChallengeFixtures.챌린지_ID, 인증_날짜);
    public static final ProofRequest.Create 잘못된_날짜_인증_생성_요청 = new ProofRequest.Create(ChallengeFixtures.챌린지_ID, 잘못된_인증_날짜);

    /* Entity */
    private static final Proof 인증_생성(LocalDate date, int round) {
        return new Proof(ChallengeFixtures.진행중인_챌린지, date, round);
    }

    public static final Proof 인증 = 인증_생성(인증_날짜, 인증_라운드);
    public static Pageable 페이지_요청 = PageRequest.of(0, 10);
    private static final List<Proof> 인증_목록 = List.of(
            인증_생성(LocalDate.of(2025, 4, 14), 1),
            인증_생성(LocalDate.of(2025, 4, 15), 2),
            인증_생성(LocalDate.of(2025, 4, 16), 3)
    );

    public static final Page<Proof> 인증_페이지 = new PageImpl<>(인증_목록, 페이지_요청, 인증_목록.size());

    /* Response DTO */
    public static final ProofResponse.Summary 인증_요약_응답 = new ProofResponse.Summary(인증_ID, 인증_날짜, 인증_라운드);
    public static final PageResponse<ProofResponse.Summary> 인증_페이지_응답 = new PageResponse<>(인증_페이지.map(ProofResponse.Summary::from));
}