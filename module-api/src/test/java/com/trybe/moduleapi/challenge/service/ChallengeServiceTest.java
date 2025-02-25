package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.fixtures.ChallengeFixtures;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {
    @BeforeTestClass
    public void init() {
        MockitoAnnotations.initMocks(this);
    }
    @InjectMocks
    private ChallengeService challengeService;

    @Mock
    private ChallengeRepository challengeRepository;

    private final Long TMP_USER_ID = 1L;

    @Test
    @DisplayName("챌린지 생성 시 저장된 챌린지 정보를 반환한다.")
    void 챌린지_생성_시_저장된_챌린지_정보를_반환한다 () {
        /* given */
        ChallengeRequest.Create request = ChallengeFixtures.챌린지_생성_요청;

        when(challengeRepository.save(any(Challenge.class)))
                .thenReturn(ChallengeFixtures.챌린지());

        /* when */
        ChallengeResponse.Detail response = challengeService.save(request, TMP_USER_ID);

        /* then */
        verifyChallengeResponse(ChallengeFixtures.챌린지(), response);
    }

    @Test
    @DisplayName("챌린지 단일 조회 시 챌린지 정보를 반환한다.")
    void 챌린지_단일_조회_시_챌린지_정보를_반환한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        Challenge challenge = ChallengeFixtures.챌린지();

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(challenge));

        /* when */
        ChallengeResponse.Detail response = challengeService.find(challengeId);

        /* then */
        verifyChallengeResponse(challenge, response);
    }

    @Test
    @DisplayName("챌린지 단일 조회 시 존재하지 않는 챌린지 ID가 주어지면 예외를 던진다.")
    void 챌린지_단일_조회_시_존재하지_않는_챌린지_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeException.class, () -> challengeService.find(challengeId));
    }

    @Test
    @DisplayName("챌린지 조회 시 요청에 따른 필터링된 챌린지 정보를 반환한다.")
    void 챌린지_조회_시_요청에_따른_필터링된_챌린지_정보를_반환한다 () {
        /* given */
        ChallengeRequest.Read request = ChallengeFixtures.챌린지_조회_요청;

        when(challengeRepository.findAllByStatusInAndCategoryIn(request.statuses(), request.categories()))
                .thenReturn(ChallengeFixtures.챌린지_목록);

        /* when */
        List<ChallengeResponse.Summary> response = challengeService.findAll(request);

        /* then */
        assertEquals(ChallengeFixtures.챌린지_목록.size(), response.size());
    }

    @Test
    @DisplayName("챌린지 정보 수정 시 수정된 챌린지 정보를 반환한다.")
    void 챌린지_정보_수정_시_수정된_챌린지_정보를_반환한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateContent request = ChallengeFixtures.챌린지_내용_수정_요청;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(ChallengeFixtures.챌린지()));

        /* when */
        ChallengeResponse.Detail response = challengeService.updateContent(challengeId, request, TMP_USER_ID);

        /* then */
        verifyChallengeResponse(ChallengeFixtures.내용_수정된_챌린지, response);
    }

    @Test
    @DisplayName("챌린지 정보 수정 시 존재하지 않는 챌린지 ID가 주어지면 예외를 던진다.")
    void 챌린지_정보_수정_시_존재하지_않는_챌린지_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateContent request = ChallengeFixtures.챌린지_내용_수정_요청;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeException.class, () -> challengeService.updateContent(challengeId, request, TMP_USER_ID));
    }

    @Test
    @DisplayName("챌린지 인증 정보 수정 시 수정된 챌린지 정보를 반환한다.")
    void 챌린지_인증_정보_수정_시_수정된_챌린지_정보를_반환한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateProof request = ChallengeFixtures.챌린지_인증_내용_수정_요청;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(ChallengeFixtures.챌린지()));

        /* when */
        ChallengeResponse.Detail response = challengeService.updateProof(challengeId, request, TMP_USER_ID);

        /* then */
        verifyChallengeResponse(ChallengeFixtures.인증_내용_수정된_챌린지, response);
    }

    @Test
    @DisplayName("챌린지 인증 정보 수정 시 존재하지 않는 챌린지 ID가 주어지면 예외를 던진다.")
    void 챌린지_인증_정보_수정_시_존재하지_않는_챌린지_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        ChallengeRequest.UpdateProof request = ChallengeFixtures.챌린지_인증_내용_수정_요청;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeException.class, () -> challengeService.updateProof(challengeId, request, TMP_USER_ID));
    }

    @Test
    @DisplayName("챌린지 삭제 시 챌린지를 삭제한다.")
    void 챌린지_삭제_시_챌린지를_삭제한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(ChallengeFixtures.챌린지()));

        /* when */
        /* then */
        challengeService.delete(challengeId, TMP_USER_ID);
    }

    @Test
    @DisplayName("챌린지 삭제 시 존재하지 않는 챌린지 ID가 주어지면 예외를 던진다.")
    void 챌린지_삭제_시_존재하지_않는_챌린지_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeException.class, () -> challengeService.delete(challengeId, TMP_USER_ID));
    }

    private void verifyChallengeResponse(Challenge challenge, ChallengeResponse.Detail response) {
        assertEquals(challenge.getTitle(), response.title());
        assertEquals(challenge.getDescription(), response.description());
        assertEquals(challenge.getStartDate(), response.startDate());
        assertEquals(challenge.getEndDate(), response.endDate());
        assertEquals(challenge.getStatus(), response.status());
        assertEquals(challenge.getCapacity(), response.capacity());
        assertEquals(challenge.getCategory(), response.category());
        assertEquals(challenge.getProofWay(), response.proofWay());
        assertEquals(challenge.getProofCount(), response.proofCount());
    }
}