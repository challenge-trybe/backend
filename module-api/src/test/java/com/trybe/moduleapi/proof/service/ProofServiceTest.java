package com.trybe.moduleapi.proof.service;

import com.trybe.moduleapi.challenge.exception.InvalidChallengeStatusException;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.challenge.exception.participation.InvalidChallengeRoleActionException;
import com.trybe.moduleapi.challenge.exception.participation.InvalidParticipationStatusActionException;
import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.proof.dto.request.ProofRequest;
import com.trybe.moduleapi.proof.dto.response.ProofResponse;
import com.trybe.moduleapi.proof.exception.DuplicatedProofException;
import com.trybe.moduleapi.proof.exception.NotFoundProofException;
import com.trybe.moduleapi.proof.exception.ProofCountExceededException;
import com.trybe.moduleapi.proof.fixtures.ProofFixtures;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.challenge.enums.ChallengeRole;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.proof.entity.Proof;
import com.trybe.modulecore.proof.repository.ProofRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.trybe.moduleapi.proof.fixtures.ProofFixtures.*;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProofServiceTest {
    @InjectMocks
    private ProofService proofService;

    @Mock
    private ProofRepository proofRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private ChallengeParticipationRepository challengeParticipationRepository;

    @Test
    @DisplayName("인증 생성 시 저장된 인증 정보를 반환한다.")
    void 인증_생성_시_저장된_인증_정보를_반환한다 () {
        /* given */
        ProofRequest.Create request = 인증_생성_요청;
        Proof proof = 인증;
        int recentRound = 0;

        when(challengeRepository.findById(request.challengeId()))
                .thenReturn(Optional.of(ChallengeFixtures.진행중인_챌린지));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(any(), any(), eq(ChallengeRole.LEADER)))
                .thenReturn(true);
        when(proofRepository.existsByChallengeIdAndDate(any(), eq(request.date())))
                .thenReturn(false);
        when(proofRepository.countByChallengeId(any()))
                .thenReturn(recentRound);
        when(proofRepository.save(any(Proof.class)))
                .thenReturn(인증);

        /* when */
        ProofResponse.Summary result = proofService.save(UserFixtures.회원, request);

        /* then */
        assertEquals(proof.getId(), result.id());
        assertEquals(request.date(), result.date());
        assertEquals(recentRound + 1., result.round());
    }

    @Test
    @DisplayName("인증 생성 시 존재하지 않는 챌린지 ID가 주어지면 예외를 던진다.")
    void 인증_생성_시_존재하지_않는_챌린지_ID가_주어지면_예외를_던진다 () {
        /* given */
        ProofRequest.Create request = 인증_생성_요청;

        when(challengeRepository.findById(request.challengeId()))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeException.class, () -> proofService.save(UserFixtures.회원, request));
    }
    
    @Test
    @DisplayName("인증 생성 시 주어진 챌린지의 리더가 아닌 경우 예외를 던진다.")
    void 인증_생성_시_주어진_챌린지의_리더가_아닌_경우_예외를_던진다 () {
        /* given */
        ProofRequest.Create request = 인증_생성_요청;

        when(challengeRepository.findById(request.challengeId()))
                .thenReturn(Optional.of(ChallengeFixtures.진행중인_챌린지));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(any(), any(), eq(ChallengeRole.LEADER)))
                .thenReturn(false);

        /* when */
        /* then */
        assertThrows(InvalidChallengeRoleActionException.class, () -> proofService.save(UserFixtures.회원, request));
    }

    @Test
    @DisplayName("인증 생성 시 진행 중인 챌린지가 아닌 경우 예외를 던진다.")
    void 인증_생성_시_진행_중인_챌린지가_아닌_경우_예외를_던진다 () {
        /* given */
        ProofRequest.Create request = 인증_생성_요청;

        when(challengeRepository.findById(request.challengeId()))
                .thenReturn(Optional.of(ChallengeFixtures.종료된_챌린지));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(any(), any(), eq(ChallengeRole.LEADER)))
                .thenReturn(true);

        /* when */
        /* then */
        assertThrows(InvalidChallengeStatusException.class, () -> proofService.save(UserFixtures.회원, request));
    }

    @Test
    @DisplayName("인증 생성 시 이미 요청한 날짜에 인증이 등록된 경우 예외를 던진다.")
    void 인증_생성_시_이미_요청한_날짜에_인증이_등록된_경우_예외를_던진다 () {
        /* given */
        ProofRequest.Create request = 인증_생성_요청;

        when(challengeRepository.findById(request.challengeId()))
                .thenReturn(Optional.of(ChallengeFixtures.진행중인_챌린지));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(any(), any(), eq(ChallengeRole.LEADER)))
                .thenReturn(true);
        when(proofRepository.existsByChallengeIdAndDate(any(), eq(request.date())))
                .thenReturn(true);

        /* when */
        /* then */
        assertThrows(DuplicatedProofException.class, () -> proofService.save(UserFixtures.회원, request));
    }

    @Test
    @DisplayName("인증 생성 시 챌린지 인증 라운드를 초과하는 경우 예외를 던진다.")
    void 인증_생성_시_챌린지_인증_라운드를_초과하는_경우_예외를_던진다 () {
        /* given */
        ProofRequest.Create request = 인증_생성_요청;

        when(challengeRepository.findById(request.challengeId()))
                .thenReturn(Optional.of(ChallengeFixtures.진행중인_챌린지));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(any(), any(), eq(ChallengeRole.LEADER)))
                .thenReturn(true);
        when(proofRepository.existsByChallengeIdAndDate(any(), eq(request.date())))
                .thenReturn(false);
        when(proofRepository.countByChallengeId(any()))
                .thenReturn(ChallengeFixtures.진행중인_챌린지.getProofCount());

        /* when */
        /* then */
        assertThrows(ProofCountExceededException.class, () -> proofService.save(UserFixtures.회원, request));
    }

    @Test
    @DisplayName("인증 단일 조회 시 인증 정보를 반환한다.")
    void 인증_단일_조회_시_인증_정보를_반환한다 () {
        /* given */
        Long proofId = 인증_ID;
        Proof proof = 인증;

        when(proofRepository.findById(proofId))
                .thenReturn(Optional.of(proof));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(true);

        /* when */
        ProofResponse.Summary result = proofService.find(UserFixtures.회원, proofId);

        /* then */
        assertEquals(proof.getId(), result.id());
        assertEquals(proof.getDate(), result.date());
        assertEquals(proof.getRound(), result.round());
    }

    @Test
    @DisplayName("인증 단일 조회 시 존재하지 않는 인증 ID가 주어지면 예외를 던진다.")
    void 인증_단일_조회_시_존재하지_않는_인증_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long proofId = 인증_ID;

        when(proofRepository.findById(proofId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundProofException.class, () -> proofService.find(UserFixtures.회원, proofId));
    }

    @Test
    @DisplayName("인증 단일 조회 시 수락된 참여자가 아닌 경우 예외를 던진다.")
    void 인증_단일_조회_시_수락된_참여자가_아닌_경우_예외를_던진다 () {
        /* given */
        Long proofId = 인증_ID;

        when(proofRepository.findById(proofId))
                .thenReturn(Optional.of(인증));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(false);

        /* when */
        /* then */
        assertThrows(InvalidParticipationStatusActionException.class, () -> proofService.find(UserFixtures.회원, proofId));
    }

    @Test
    @DisplayName("인증 목록 조회 시 주어진 챌린지에 대한 인증 목록을 반환한다.")
    void 인증_목록_조회_시_주어진_챌린지에_대한_인증_목록을_반환한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(ChallengeFixtures.진행중인_챌린지));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(true);
        when(proofRepository.findAllByChallengeId(challengeId, ProofFixtures.페이지_요청))
                .thenReturn(인증_페이지);

        /* when */
        PageResponse<ProofResponse.Summary> result = proofService.findAll(UserFixtures.회원, challengeId, ProofFixtures.페이지_요청);

        /* then */
        assertEquals(인증_페이지_응답.totalElements(), result.totalElements());
    }

    @Test
    @DisplayName("인증 목록 조회 시 존재하지 않는 챌린지 ID가 주어지면 예외를 던진다.")
    void 인증_목록_조회_시_존재하지_않는_챌린지_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeException.class, () -> proofService.findAll(UserFixtures.회원, challengeId, ProofFixtures.페이지_요청));
    }

    @Test
    @DisplayName("인증 목록 조회 시 수락된 참여자가 아닌 경우 예외를 던진다.")
    void 인증_목록_조회_시_수락된_참여자가_아닌_경우_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(ChallengeFixtures.진행중인_챌린지));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(false);

        /* when */
        /* then */
        assertThrows(InvalidParticipationStatusActionException.class, () -> proofService.findAll(UserFixtures.회원, challengeId, ProofFixtures.페이지_요청));
    }

    @Test
    @DisplayName("인증 삭제 시 인증을 삭제한다.")
    void 인증_삭제_시_인증을_삭제한다 () {
        /* given */
        Long proofId = 인증_ID;

        when(proofRepository.findById(proofId))
                .thenReturn(Optional.of(인증));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(any(), any(), eq(ChallengeRole.LEADER)))
                .thenReturn(true);

        /* when */
        /* then */
        proofService.delete(UserFixtures.회원, proofId);

        verify(proofRepository, atLeastOnce()).delete(any(Proof.class));
    }

    @Test
    @DisplayName("인증 삭제 시 존재하지 않는 인증 ID가 주어지면 예외를 던진다.")
    void 인증_삭제_시_존재하지_않는_인증_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long proofId = 인증_ID;

        when(proofRepository.findById(proofId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundProofException.class, () -> proofService.delete(UserFixtures.회원, proofId));
    }

    @Test
    @DisplayName("인증 삭제 시 챌린지의 리더가 아닌 경우 예외를 던진다")
    void 인증_삭제_시_챌린지의_리더가_아닌_경우_예외를_던진다 () {
        /* given */
        Long proofId = 인증_ID;

        when(proofRepository.findById(proofId))
                .thenReturn(Optional.of(인증));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(any(), any(), eq(ChallengeRole.LEADER)))
                .thenReturn(false);

        /* when */
        /* then */
        assertThrows(InvalidChallengeRoleActionException.class, () -> proofService.delete(UserFixtures.회원, proofId));
    }
}