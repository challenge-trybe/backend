package com.trybe.moduleapi.challenge.fixtures;

import com.trybe.moduleapi.challenge.dto.ChallengeParticipationResponse;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.challenge.enums.ChallengeRole;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public class ChallengeParticipationFixtures {
    public static final Long 챌린지_참여_ID = 1L;
    public static final Long 잘못된_챌린지_참여_ID = 0L;

    public static final ChallengeRole 챌린지_리더_역할 = ChallengeRole.LEADER;
    public static final ChallengeRole 챌린지_멤버_역할 = ChallengeRole.MEMBER;

    public static final ParticipationStatus 챌린지_참여_대기_상태 = ParticipationStatus.PENDING;
    public static final ParticipationStatus 챌린지_참여_수락_상태 = ParticipationStatus.ACCEPTED;
    public static final ParticipationStatus 챌린지_참여_거절_상태 = ParticipationStatus.REJECTED;
    public static final ParticipationStatus 챌린지_참여_탈퇴_상태 = ParticipationStatus.DISABLED;

    public static final int 챌린지_참여_대기_최대_수 = 20;

    /* Entity */
    public static User 리더 = UserFixtures.회원_생성("leader", "leader@test.com");
    public static User 멤버 = UserFixtures.회원_생성("member", "member@test.com");
    public static User 다른_멤버 = UserFixtures.회원_생성("another", "another@test.com");

    public static ChallengeParticipation 챌린지_참여(User user, Challenge challenge, ChallengeRole role, ParticipationStatus status) {
        return new ChallengeParticipation(user, challenge, role, status);
    }

    public static ChallengeParticipation 챌린지_리더_참여() { return 챌린지_참여(리더, ChallengeFixtures.챌린지(), 챌린지_리더_역할, 챌린지_참여_수락_상태); }
    public static ChallengeParticipation 챌린지_멤버_참여() { return 챌린지_참여(멤버, ChallengeFixtures.챌린지(), 챌린지_멤버_역할, 챌린지_참여_수락_상태); }
    public static ChallengeParticipation 챌린지_멤버_참여_대기() { return 챌린지_참여(멤버, ChallengeFixtures.챌린지(), 챌린지_멤버_역할, 챌린지_참여_대기_상태); }
    public static ChallengeParticipation 진행중인_챌린지_멤버_참여_대기() { return 챌린지_참여(멤버, ChallengeFixtures.진행중인_챌린지, 챌린지_멤버_역할, 챌린지_참여_대기_상태); }

    public static Pageable 페이징_요청 = PageRequest.of(0, 10);

    private static List<ChallengeParticipation> 나의_참여_중인_챌린지_목록 = List.of(챌린지_멤버_참여());
    private static List<ChallengeParticipation> 챌린지_참여_목록 = List.of(챌린지_리더_참여(), 챌린지_멤버_참여());
    private static List<ChallengeParticipation> 챌린지_참여_신청_목록 = List.of(챌린지_멤버_참여_대기());

    public static Page<ChallengeParticipation> 나의_참여_중인_챌린지_목록_페이지 = new PageImpl<>(나의_참여_중인_챌린지_목록, 페이징_요청, 나의_참여_중인_챌린지_목록.size());
    public static Page<ChallengeParticipation> 챌린지_참여_목록_페이지 = new PageImpl<>(챌린지_참여_목록, 페이징_요청, 챌린지_참여_목록.size());
    public static Page<ChallengeParticipation> 챌린지_참여_신청_목록_페이지 = new PageImpl<>(챌린지_참여_신청_목록, 페이징_요청, 챌린지_참여_신청_목록.size());

    /* Response DTO */
    public static ChallengeParticipationResponse.Detail 챌린지_참여_상세_응답() {
        return new ChallengeParticipationResponse.Detail(ChallengeFixtures.챌린지_요약_응답, 챌린지_참여_ID, UserFixtures.회원_응답, 챌린지_멤버_역할, 챌린지_참여_대기_상태, LocalDateTime.now());
    }

    public static ChallengeParticipationResponse.Summary 챌린지_참여_요약_리더() {
        return new ChallengeParticipationResponse.Summary(챌린지_참여_ID, UserFixtures.회원_응답, 챌린지_리더_역할, 챌린지_참여_수락_상태, LocalDateTime.now());
    }

    public static ChallengeParticipationResponse.Summary 챌린지_참여_요약_멤버() {
        return new ChallengeParticipationResponse.Summary(챌린지_참여_ID, UserFixtures.회원_응답, 챌린지_멤버_역할, 챌린지_참여_수락_상태, LocalDateTime.now());
    }

    public static PageResponse<ChallengeParticipationResponse.Summary> 나의_참여_중인_챌린지_목록_페이지_응답 = new PageResponse<>(나의_참여_중인_챌린지_목록_페이지.map(ChallengeParticipationResponse.Summary::from));
    public static PageResponse<ChallengeParticipationResponse.Summary> 챌린지_참여_목록_페이지_응답 = new PageResponse<>(챌린지_참여_목록_페이지.map(ChallengeParticipationResponse.Summary::from));
    public static PageResponse<ChallengeParticipationResponse.Summary> 챌린지_참여_신청_목록_페이지_응답 = new PageResponse<>(챌린지_참여_신청_목록_페이지.map(ChallengeParticipationResponse.Summary::from));
}
