package com.trybe.moduleapi.fixtures;

import com.trybe.moduleapi.challenge.dto.ChallengeRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ChallengeCategory;
import com.trybe.modulecore.challenge.enums.ChallengeStatus;

import java.time.LocalDate;
import java.util.List;

public class ChallengeFixtures {
    public static final Long 챌린지_ID = 1L;
    public static final Long 잘못된_챌린지_ID = 0L;

    public static final String 챌린지_제목 = "도파민 디톡스";
    public static final String 잘못된_챌린지_제목 = "쇼츠 하나만 보고 자려고 했던 당신, 어느새 새벽 3시. 우리의 뇌는 점점 피로해지고 있다. 하지만 우리는 이렇게 계속 살아도 괜찮을까? 7일 도파민 디톡스 챌린지로 집중력을 되찾는 방법을 지금 시작하세요!";
    public static final String 수정된_챌린지_제목 = "수정된 도파민 디톡스";

    public static final String 챌린지_설명 = "도파민에 절여진 여러분들을 위한 챌린지입니다. 7일동안 매일 도파민에서 해방된 삶을 즐겨봐요!";
    public static final String 잘못된_챌린지_설명 = " ";
    public static final String 수정된_챌린지_설명 = "도파민에 절여진 여러분들을 위한 챌린지입니다. 14일동안 매일 도파민에서 해방된 삶을 즐겨봐요!";

    public static final LocalDate 챌린지_시작_날짜 = LocalDate.of(2025, 3, 1);
    public static final LocalDate 잘못된_챌린지_시작_날짜 = LocalDate.of(2001, 5, 12);
    public static final LocalDate 수정된_챌린지_시작_날짜 = LocalDate.of(2025, 3, 2);

    public static final LocalDate 챌린지_종료_날짜 = LocalDate.of(2025, 3, 7);
    public static final LocalDate 잘못된_챌린지_종료_날짜 = LocalDate.of(2001, 11, 14);
    public static final LocalDate 수정된_챌린지_종료_날짜 = LocalDate.of(2025, 3, 15);

    public static final int 챌린지_인원 = 6;
    public static final int 잘못된_챌린지_인원 = 30;
    public static final int 수정된_챌린지_인원 = 8;

    public static final ChallengeCategory 챌린지_카테고리 = ChallengeCategory.HOBBY;
    public static final ChallengeCategory 수정된_챌린지_카테고리 = ChallengeCategory.LIFE;

    public static final String 챌린지_인증_방법 = "스크린 타임 캡처해서 업로드하기";
    public static final String 잘못된_챌린지_인증_방법 = " ";
    public static final String 수정된_챌린지_인증_방법 = "스크린 타임의 인스타, 유튜브 사용 시간 캡처해서 업로드하기";

    public static final int 챌린지_인증_횟수 = 7;
    public static final int 잘못된_챌린지_인증_횟수 = 0;
    public static final int 수정된_챌린지_인증_횟수 = 14;

    /* Request DTO */
    public static final ChallengeRequest.Create 챌린지_생성_요청 = new ChallengeRequest.Create(
            챌린지_제목,
            챌린지_설명,
            챌린지_시작_날짜,
            챌린지_종료_날짜,
            챌린지_인원,
            챌린지_카테고리,
            챌린지_인증_방법,
            챌린지_인증_횟수
    );

    public static final ChallengeRequest.Create 잘못된_챌린지_생성_요청 = new ChallengeRequest.Create(
            잘못된_챌린지_제목,
            잘못된_챌린지_설명,
            잘못된_챌린지_시작_날짜,
            잘못된_챌린지_종료_날짜,
            잘못된_챌린지_인원,
            챌린지_카테고리,
            잘못된_챌린지_인증_방법,
            잘못된_챌린지_인증_횟수
    );

    public static final ChallengeRequest.Read 챌린지_조회_요청 = new ChallengeRequest.Read(
            List.of(ChallengeStatus.PENDING, ChallengeStatus.ONGOING),
            List.of(ChallengeCategory.LIFE)
    );

    public static final ChallengeRequest.Read 잘못된_챌린지_조회_요청 = new ChallengeRequest.Read(
            List.of(),
            List.of()
    );

    public static final ChallengeRequest.UpdateContent 챌린지_내용_수정_요청 = new ChallengeRequest.UpdateContent(
            수정된_챌린지_제목,
            수정된_챌린지_설명,
            수정된_챌린지_시작_날짜,
            수정된_챌린지_종료_날짜,
            수정된_챌린지_인원,
            수정된_챌린지_카테고리
    );

    public static final ChallengeRequest.UpdateContent 잘못된_챌린지_내용_수정_요청 = new ChallengeRequest.UpdateContent(
            잘못된_챌린지_제목,
            잘못된_챌린지_설명,
            잘못된_챌린지_시작_날짜,
            잘못된_챌린지_종료_날짜,
            잘못된_챌린지_인원,
            챌린지_카테고리
    );

    public static final ChallengeRequest.UpdateProof 챌린지_인증_내용_수정_요청 = new ChallengeRequest.UpdateProof(
            수정된_챌린지_인증_방법,
            수정된_챌린지_인증_횟수
    );

    public static final ChallengeRequest.UpdateProof 잘못된_챌린지_인증_내용_수정_요청 = new ChallengeRequest.UpdateProof(
            잘못된_챌린지_인증_방법,
            잘못된_챌린지_인증_횟수
    );

    /* Entity */
    public static final Challenge 챌린지() {
        return new Challenge(
                챌린지_제목,
                챌린지_설명,
                챌린지_시작_날짜,
                챌린지_종료_날짜,
                챌린지_인원,
                챌린지_카테고리,
                챌린지_인증_방법,
                챌린지_인증_횟수
        );
    }

    public static final Challenge 내용_수정된_챌린지 = new Challenge(
            수정된_챌린지_제목,
            수정된_챌린지_설명,
            수정된_챌린지_시작_날짜,
            수정된_챌린지_종료_날짜,
            수정된_챌린지_인원,
            수정된_챌린지_카테고리,
            챌린지_인증_방법,
            챌린지_인증_횟수
    );
    public static final Challenge 인증_내용_수정된_챌린지 = new Challenge(
            챌린지_제목,
            챌린지_설명,
            챌린지_시작_날짜,
            챌린지_종료_날짜,
            챌린지_인원,
            챌린지_카테고리,
            수정된_챌린지_인증_방법,
            수정된_챌린지_인증_횟수
    );

    private static Challenge createChallenge(ChallengeStatus status) {
        Challenge challenge = new Challenge(
                챌린지_제목,
                챌린지_설명,
                챌린지_시작_날짜,
                챌린지_종료_날짜,
                챌린지_인원,
                수정된_챌린지_카테고리,
                챌린지_인증_방법,
                챌린지_인증_횟수
        );
        challenge.updateStatus(status);
        return challenge;
    }

    public static Challenge 진행중인_챌린지 = createChallenge(ChallengeStatus.ONGOING);
    public static Challenge 종료된_챌린지 = createChallenge(ChallengeStatus.DONE);

    public static List<Challenge> 챌린지_목록 = List.of(진행중인_챌린지);

    public static ChallengeStatus 대기중 = ChallengeStatus.PENDING;

    /* Response DTO */
    public static final ChallengeResponse.Detail 챌린지_상세_응답 = ChallengeResponse.Detail.from(챌린지());
    public static final ChallengeResponse.Summary 챌린지_요약_응답 = ChallengeResponse.Summary.from(챌린지());

    public static final ChallengeResponse.Detail 내용_수정된_챌린지_상세_응답 = ChallengeResponse.Detail.from(내용_수정된_챌린지);
    public static final ChallengeResponse.Detail 인증_내용_수정된_챌린지_상세_응답 = ChallengeResponse.Detail.from(인증_내용_수정된_챌린지);

    public static final List<ChallengeResponse.Summary> 챌린지_목록_응답 = 챌린지_목록.stream().map(ChallengeResponse.Summary::from).toList();
}
