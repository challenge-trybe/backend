package com.trybe.moduleapi.proof.fixtures;

import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.proof.dto.request.ProofHistoryRequest;
import com.trybe.moduleapi.proof.dto.response.ProofHistoryResponse;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.proof.entity.Proof;
import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.proof.enums.ProofHistoryStatus;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProofHistoryFixtures {
    public static final Long 인증_기록_ID = 1L;

    public static final String 인증_기록_내용 = "인증 기록 내용";
    public static final String 수정된_인증_기록_내용 = "수정된 인증 기록 내용";
    public static final String 잘못된_인증_기록_내용 = "";

    public static final ProofHistoryStatus 인증_기록_대기_상태 = ProofHistoryStatus.PENDING;
    public static final ProofHistoryStatus 인증_기록_성공_상태 = ProofHistoryStatus.PASSED;
    public static final ProofHistoryStatus 인증_기록_실패_상태 = ProofHistoryStatus.FAILED;

    public static final LocalDateTime 인증_기록_생성_시간 = LocalDateTime.now();

    /* Request DTO */
    public static final ProofHistoryRequest.Create 인증_기록_생성_요청 = new ProofHistoryRequest.Create(인증_기록_내용);
    public static final ProofHistoryRequest.Update 인증_기록_수정_요청 = new ProofHistoryRequest.Update(수정된_인증_기록_내용);

    public static final ProofHistoryRequest.Create 잘못된_인증_기록_생성_요청 = new ProofHistoryRequest.Create(잘못된_인증_기록_내용);
    public static final ProofHistoryRequest.Update 잘못된_인증_기록_수정_요청 = new ProofHistoryRequest.Update(잘못된_인증_기록_내용);

    /* Entity */
    private static ProofHistory 인증_기록_생성(Proof proof, User user, String content, ProofHistoryStatus status) {
        ProofHistory proofHistory = new ProofHistory(proof, user, content);
        proofHistory.updateStatus(status);
        return proofHistory;
    }

    public static final Proof 오늘_인증 = ProofFixtures.인증_생성(LocalDate.now(), ProofFixtures.인증_라운드);
    public static final Proof 내일_인증 = ProofFixtures.인증_생성(LocalDate.now().plusDays(1), ProofFixtures.인증_라운드);
    public static final ProofHistory 대기_인증_기록 = 인증_기록_생성(오늘_인증, UserFixtures.회원, 인증_기록_내용, 인증_기록_대기_상태);
    public static final ProofHistory 성공_인증_기록 = 인증_기록_생성(오늘_인증, UserFixtures.회원, 인증_기록_내용, 인증_기록_성공_상태);
    public static final ProofHistory 실패_인증_기록 = 인증_기록_생성(오늘_인증, UserFixtures.회원, 인증_기록_내용, 인증_기록_실패_상태);

    public static Pageable 페이지_요청 = PageRequest.of(0, 10);

    private static final List<ProofHistory> 인증_기록_목록 = List.of(
            인증_기록_생성(오늘_인증, UserFixtures.회원, 인증_기록_내용, 인증_기록_대기_상태),
            인증_기록_생성(오늘_인증, UserFixtures.회원, 인증_기록_내용, 인증_기록_대기_상태),
            인증_기록_생성(오늘_인증, UserFixtures.회원, 인증_기록_내용, 인증_기록_대기_상태)
    );

    public static final Page<ProofHistory> 인증_기록_목록_페이지 = new PageImpl<>(인증_기록_목록, 페이지_요청, 인증_기록_목록.size());

    /* Response DTO */
    public static final ProofHistoryResponse.Summary 대기_인증_기록_요약_응답 = new ProofHistoryResponse.Summary(인증_기록_ID, 인증_기록_내용, 인증_기록_대기_상태, 인증_기록_생성_시간);
    public static final ProofHistoryResponse.Summary 성공_인증_기록_요약_응답 = new ProofHistoryResponse.Summary(인증_기록_ID, 인증_기록_내용, 인증_기록_성공_상태, 인증_기록_생성_시간);
    public static final ProofHistoryResponse.Summary 실패_인증_기록_요약_응답 = new ProofHistoryResponse.Summary(인증_기록_ID, 인증_기록_내용, 인증_기록_실패_상태, 인증_기록_생성_시간);

    public static final PageResponse<ProofHistoryResponse.Summary> 인증_기록_목록_페이지_응답 = new PageResponse<>(인증_기록_목록_페이지.map(ProofHistoryResponse.Summary::from));
}