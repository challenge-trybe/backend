package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeParticipationResponse;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.exception.InvalidChallengeStatusException;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.challenge.exception.participation.*;
import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.trybe.moduleapi.challenge.fixtures.ChallengeParticipationFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeParticipationServiceTest {
    @InjectMocks
    private ChallengeParticipationService challengeParticipationService;

    @Mock
    private ChallengeParticipationRepository challengeParticipationRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    private Long 멤버_ID = 1L;
    private Long 다른_멤버_ID = 2L;

    @Test
    @DisplayName("챌린지 참여 신청 시 저장된 챌린지 참여 정보를 반환한다.")
    void 챌린지_참여_신청_시_저장된_챌린지_참여_정보를_반환한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        Challenge challenge = ChallengeFixtures.챌린지();
        ChallengeParticipation participation = 챌린지_멤버_참여_대기();

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(challenge));
        when(challengeParticipationRepository.existsByUserIdAndChallengeId(멤버.getId(), challengeId))
                .thenReturn(false);
        when(challengeParticipationRepository.countByChallengeIdAndStatus(any(), eq(챌린지_참여_수락_상태)))
                .thenReturn(challenge.getCapacity() - 1);
        when(challengeParticipationRepository.countByChallengeIdAndStatus(any(), eq(챌린지_참여_대기_상태)))
                .thenReturn(챌린지_참여_대기_최대_수 - 1);
        when(challengeParticipationRepository.save(any(ChallengeParticipation.class)))
                .thenReturn(participation);

        /* when */
        ChallengeParticipationResponse.Detail result = challengeParticipationService.join(멤버, challengeId);

        /* then */
        verifyChallengeParticipationResponse(participation, result);
        verifyChallengeResponseSummary(challenge, result.challenge());
        verifyUserResponse(멤버, result.user());
    }

    @Test
    @DisplayName("챌린지 참여 신청 시 존재하지 않는 챌린지 ID가 주어지면 예외를 던진다.")
    void 챌린지_참여_신청_시_존재하지_않는_챌린지_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeException.class, () -> challengeParticipationService.join(멤버, challengeId));
    }

    @Test
    @DisplayName("챌린지 참여 신청 시 이미 챌린지 참여 정보가 존재하면 예외를 던진다.")
    void 챌린지_참여_신청_시_이미_챌린지_참여_정보가_존재하면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(ChallengeFixtures.챌린지()));
        when(challengeParticipationRepository.existsByUserIdAndChallengeId(멤버.getId(), challengeId))
                .thenReturn(true);

        /* when */
        /* then */
        assertThrows(DuplicatedChallengeParticipationException.class, () -> challengeParticipationService.join(멤버, challengeId));
    }

    @Test
    @DisplayName("챌린지 참여 신청 시 진행 예정 상태의 챌린지가 아니면 예외를 던진다.")
    void 챌린지_참여_신청_시_진행_예정_상태의_챌린지가_아니면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        Challenge challenge = ChallengeFixtures.진행중인_챌린지;

        /* when */
        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(challenge));
        when(challengeParticipationRepository.existsByUserIdAndChallengeId(멤버.getId(), challengeId))
                .thenReturn(false);

        /* then */
        assertThrows(InvalidChallengeStatusException.class, () -> challengeParticipationService.join(멤버, challengeId));
    }

    @Test
    @DisplayName("챌린지 참여 신청 시 챌린지 참여 인원이 꽉 찼으면 예외를 던진다.")
    void 챌린지_참여_신청_시_챌린지_참여_인원이_꽉_찼으면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        Challenge challenge = ChallengeFixtures.챌린지();

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(challenge));
        when(challengeParticipationRepository.existsByUserIdAndChallengeId(멤버.getId(), challengeId))
                .thenReturn(false);
        when(challengeParticipationRepository.countByChallengeIdAndStatus(any(), eq(챌린지_참여_수락_상태)))
                .thenReturn(challenge.getCapacity());

        /* when */
        /* then */
        assertThrows(ChallengeFullException.class, () -> challengeParticipationService.join(멤버, challengeId));
    }

    @Test
    @DisplayName("챌린지 참여 신청 시 챌린지 참여 신청 대기열이 꽉 찼으면 예외를 던진다.")
    void 챌린지_참여_신청_시_챌린지_참여_신청_대기열이_꽉_찼으면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        Challenge challenge = ChallengeFixtures.챌린지();

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(challenge));
        when(challengeParticipationRepository.existsByUserIdAndChallengeId(멤버.getId(), challengeId))
                .thenReturn(false);
        when(challengeParticipationRepository.countByChallengeIdAndStatus(any(), eq(챌린지_참여_수락_상태)))
                .thenReturn(challenge.getCapacity() - 1);
        when(challengeParticipationRepository.countByChallengeIdAndStatus(any(), eq(챌린지_참여_대기_상태)))
                .thenReturn(챌린지_참여_대기_최대_수);

        /* when */
        /* then */
        assertThrows(ChallengeParticipationFullException.class, () -> challengeParticipationService.join(멤버, challengeId));
    }

    @Test
    @DisplayName("나의 챌린지 참여 목록 조회 시 챌린지 참여 정보를 반환한다.")
    void 나의_챌린지_참여_목록_조회_시_챌린지_참여_정보를_반환한다 () {
        /* given */
        when(challengeParticipationRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(멤버.getId(), 챌린지_참여_수락_상태, 페이징_요청))
                .thenReturn((나의_참여_중인_챌린지_목록_페이지));

        /* when */
        PageResponse<ChallengeParticipationResponse.Detail> result = challengeParticipationService.getMyParticipations(멤버, 챌린지_참여_수락_상태, 페이징_요청);

        /* then */
        assertEquals(나의_참여_중인_챌린지_목록_페이지.getTotalElements(), result.totalElements());
        assertEquals(나의_참여_중인_챌린지_목록_페이지.getContent().size(), result.content().size());
    }
    
    @Test
    @DisplayName("챌린지 참여자 목록 조회 시 챌린지 참여 정보를 반환한다.")
    void 챌린지_참여자_목록_조회_시_챌린지_참여_정보를_반환한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        Challenge challenge = ChallengeFixtures.챌린지();

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(challenge));
        when(challengeParticipationRepository.findByUserIdAndChallengeId(any(), any()))
                .thenReturn(Optional.of(챌린지_멤버_참여()));
        when(challengeParticipationRepository.findAllByChallengeIdAndStatusOrderByCreatedAtAsc(challengeId, 챌린지_참여_수락_상태, 페이징_요청))
                .thenReturn(챌린지_참여_목록_페이지);

        /* when */
        PageResponse<ChallengeParticipationResponse.Summary> result = challengeParticipationService.getParticipants(멤버, challengeId, 챌린지_참여_수락_상태, 페이징_요청);
        
        /* then */
        assertEquals(챌린지_참여_목록_페이지.getTotalElements(), result.totalElements());
        assertEquals(챌린지_참여_목록_페이지.getContent().size(), result.content().size());
    }

    @Test
    @DisplayName("챌린지 참여자 목록 조회 시 존재하지 않는 챌린지 ID가 주어지면 예외를 던진다.")
    void 챌린지_참여자_목록_조회_시_존재하지_않는_챌린지_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeException.class, () -> challengeParticipationService.getParticipants(멤버, challengeId, 챌린지_참여_수락_상태, 페이징_요청));
    }
    
    @Test
    @DisplayName("챌린지 참여자 목록 조회 시 수락된 참여자가 아니라면 예외를 던진다.")
    void 챌린지_참여자_목록_조회_시_수락된_참여자가_아니라면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        Challenge challenge = ChallengeFixtures.챌린지();

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(challenge));
        when(challengeParticipationRepository.findByUserIdAndChallengeId(any(), any()))
                .thenReturn(Optional.of(챌린지_멤버_참여_대기()));

        /* when */
        /* then */
        assertThrows(InvalidParticipationStatusActionException.class, () -> challengeParticipationService.getParticipants(멤버, challengeId, 챌린지_참여_수락_상태, 페이징_요청));
    }
    
    @Test
    @DisplayName("챌린지 참여자 목록 조회 시 리더가 아닌 사용자가 수락된 참여자 외의 조회를 수행하면 예외를 던진다.")
    void 챌린지_참여자_목록_조회_시_리더가_아닌_사용자가_수락된_참여자_외의_조회를_수행하면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        Challenge challenge = ChallengeFixtures.챌린지();

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(challenge));
        when(challengeParticipationRepository.findByUserIdAndChallengeId(any(), any()))
                .thenReturn(Optional.of(챌린지_멤버_참여()));

        /* when */
        /* then */
        assertThrows(InvalidChallengeRoleActionException.class, () -> challengeParticipationService.getParticipants(멤버, challengeId, 챌린지_참여_대기_상태, 페이징_요청));
    }
    
    @Test
    @DisplayName("챌린지 참여 처리 시 처리된 챌린지 참여 정보를 반환한다.")
    void 챌린지_참여_처리_시_처리된_챌린지_참여_정보를_반환한다 () {
        /* given */
        Long participationId = 챌린지_참여_ID;

        when(challengeParticipationRepository.findById(participationId))
                .thenReturn(Optional.of(챌린지_멤버_참여_대기()));
        when(challengeParticipationRepository.findByUserIdAndChallengeId(any(), any()))
                .thenReturn(Optional.of(챌린지_리더_참여()));
        when(challengeParticipationRepository.countByChallengeIdAndStatus(any(), eq(챌린지_참여_수락_상태)))
                .thenReturn(ChallengeFixtures.챌린지().getCapacity() - 1);

        /* when */
        ChallengeParticipationResponse.Detail result = challengeParticipationService.confirm(리더, participationId, 챌린지_참여_수락_상태);

        /* then */
        verifyChallengeParticipationResponse(챌린지_멤버_참여(), result);
    }
    
    @Test
    @DisplayName("챌린지 참여 처리 시 존재하지 않는 챌린지 참여 ID가 주어지면 예외를 던진다.")
    void 챌린지_참여_처리_시_존재하지_않는_챌린지_참여_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long participation = 챌린지_참여_ID;

        when(challengeParticipationRepository.findById(participation))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeParticipationException.class, () -> challengeParticipationService.confirm(리더, participation, 챌린지_참여_수락_상태));
    }
    
    @Test
    @DisplayName("챌린지 참여 처리 시 챌린지에 대한 참여자가 아니라면 예외를 던진다.")
    void 챌린지_참여_처리_시_챌린지에_대한_참여자가_아니라면_예외를_던진다 () {
        /* given */
        Long participationId = 챌린지_참여_ID;

        when(challengeParticipationRepository.findById(participationId))
                .thenReturn(Optional.of(챌린지_멤버_참여_대기()));
        when(challengeParticipationRepository.findByUserIdAndChallengeId(any(), any()))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeParticipationException.class, () -> challengeParticipationService.confirm(멤버, participationId, 챌린지_참여_수락_상태));
    }
    
    @Test
    @DisplayName("챌린지 참여 처리 시 리더가 아니라면 예외를 던진다.")
    void 챌린지_참여_처리_시_리더가_아니라면_예외를_던진다 () {
        /* given */
        Long participationId = 챌린지_참여_ID;

        when(challengeParticipationRepository.findById(participationId))
                .thenReturn(Optional.of(챌린지_멤버_참여_대기()));
        when(challengeParticipationRepository.findByUserIdAndChallengeId(any(), any()))
                .thenReturn(Optional.of(챌린지_멤버_참여()));
        
        /* when */
        /* then */
        assertThrows(InvalidChallengeRoleActionException.class, () -> challengeParticipationService.confirm(멤버, participationId, 챌린지_참여_수락_상태));
    }
    
    @Test
    @DisplayName("챌린지 참여 처리 시 챌린지가 진행 예정 상태가 아니라면 예외를 던진다.")
    void 챌린지_참여_처리_시_챌린지가_진행_예정_상태가_아니라면_예외를_던진다 () {
        /* given */
        Long participationId = 챌린지_참여_ID;

        when(challengeParticipationRepository.findById(participationId))
                .thenReturn(Optional.of(진행중인_챌린지_멤버_참여_대기()));
        when(challengeParticipationRepository.findByUserIdAndChallengeId(any(), any()))
                .thenReturn(Optional.of(챌린지_리더_참여()));
        
        /* when */
        /* then */
        assertThrows(InvalidChallengeStatusException.class, () -> challengeParticipationService.confirm(리더, participationId, 챌린지_참여_수락_상태));
    }
    
    @Test
    @DisplayName("챌린지 참여 처리 시 수락된 챌린지 참여 인원이 꽉 찼으면 예외를 던진다.")
    void 챌린지_참여_처리_시_수락된_챌린지_참여_인원이_꽉_찼으면_예외를_던진다 () {
        /* given */
        Long participationId = 챌린지_참여_ID;

        when(challengeParticipationRepository.findById(participationId))
                .thenReturn(Optional.of(챌린지_멤버_참여_대기()));
        when(challengeParticipationRepository.findByUserIdAndChallengeId(any(), any()))
                .thenReturn(Optional.of(챌린지_리더_참여()));
        when(challengeParticipationRepository.countByChallengeIdAndStatus(any(), eq(챌린지_참여_수락_상태)))
                .thenReturn(ChallengeFixtures.챌린지().getCapacity());

        /* when */
        /* then */
        assertThrows(ChallengeFullException.class, () -> challengeParticipationService.confirm(리더, participationId, 챌린지_참여_수락_상태));
    }

    @Test
    @DisplayName("챌린지 참여 처리 시 처리하려는 챌린지 참여 상태가 대기 중이 아니라면 예외를 던진다.")
    void 챌린지_참여_처리_시_처리하려는_챌린지_참여_상태가_대기_중이_아니라면_예외를_던진다 () {
        /* given */
        Long participationId = 챌린지_참여_ID;

        when(challengeParticipationRepository.findById(participationId))
                .thenReturn(Optional.of(챌린지_멤버_참여()));
        when(challengeParticipationRepository.findByUserIdAndChallengeId(any(), any()))
                .thenReturn(Optional.of(챌린지_리더_참여()));

        /* when */
        /* then */
        assertThrows(InvalidParticipationStatusException.class, () -> challengeParticipationService.confirm(리더, participationId, 챌린지_참여_수락_상태));
    }

    @Test
    @DisplayName("챌린지 참여 처리 시 처리 상태가 수락 혹은 거절이 아니라면 예외를 던진다.")
    void 챌린지_참여_처리_시_처리_상태가_수락_혹은_거절이_아니라면_예외를_던진다 () {
        /* given */
        Long participationId = 챌린지_참여_ID;

        when(challengeParticipationRepository.findById(participationId))
                .thenReturn(Optional.of(챌린지_멤버_참여_대기()));
        when(challengeParticipationRepository.findByUserIdAndChallengeId(any(), any()))
                .thenReturn(Optional.of(챌린지_리더_참여()));

        /* when */
        /* then */
        assertThrows(InvalidParticipationStatusException.class, () -> challengeParticipationService.confirm(리더, participationId, 챌린지_참여_대기_상태));
    }

    @Test
    @DisplayName("챌린지 탈퇴 시 챌린지 참여 정보를 비활성화한다.")
    void 챌린지_탈퇴_시_챌린지_참여_정보를_비활성화한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeParticipationRepository.findByUserIdAndChallengeId(any(), any()))
                .thenReturn(Optional.of(챌린지_멤버_참여()));

        /* when */
        /* then */
        challengeParticipationService.leave(멤버, challengeId);
    }

    @Test
    @DisplayName("챌린지 탈퇴 시 챌린지 참여 정보가 존재하지 않으면 예외를 던진다.")
    void 챌린지_탈퇴_시_챌린지_참여_정보가_존재하지_않으면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeParticipationRepository.findByUserIdAndChallengeId(any(), any()))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeParticipationException.class, () -> challengeParticipationService.leave(멤버, challengeId));
    }

    @Test
    @DisplayName("챌린지 탈퇴 시 리더라면 예외를 던진다.")
    void 챌린지_탈퇴_시_리더라면_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;

        when(challengeParticipationRepository.findByUserIdAndChallengeId(any(), eq(challengeId)))
                .thenReturn(Optional.of(챌린지_리더_참여()));

        /* when */
        /* then */
        assertThrows(InvalidChallengeRoleActionException.class, () -> challengeParticipationService.leave(리더, challengeId));
    }
    
    @Test
    @DisplayName("챌린지 참여 신청 취소 시 챌린지 참여 정보를 삭제한다.")
    void 챌린지_참여_신청_취소_시_챌린지_참여_정보를_삭제한다 () {
        /* given */
        Long participationId = 챌린지_참여_ID;
        User member = spy(멤버);

        when(member.getId()).thenReturn(멤버_ID);

        when(challengeParticipationRepository.findById(participationId))
                .thenReturn(Optional.of(챌린지_참여(member, ChallengeFixtures.챌린지(), 챌린지_멤버_역할, 챌린지_참여_대기_상태)));

        /* when */
        /* then */
        challengeParticipationService.cancel(member, participationId);
        verify(challengeParticipationRepository, atLeastOnce()).delete(any(ChallengeParticipation.class));
    }

    @Test
    @DisplayName("챌린지 참여 신청 취소 시 챌린지 참여가 존재하지 않으면 예외를 던진다.")
    void 챌린지_참여_신청_취소_시_챌린지_참여가_존재하지_않으면_예외를_던진다 () {
        /* given */
        Long participationId = 챌린지_참여_ID;

        when(challengeParticipationRepository.findById(participationId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeParticipationException.class, () -> challengeParticipationService.cancel(멤버, participationId));
    }

    @Test
    @DisplayName("챌린지 참여 신청 취소 시 챌린지 참여 신청자가 아니라면 예외를 던진다.")
    void 챌린지_참여_신청_취소_시_챌린지_참여_신청자가_아니라면_예외를_던진다 () {
        /* given */
        Long participationId = 챌린지_참여_ID;
        User member = spy(멤버);
        User another = spy(다른_멤버);

        when(member.getId()).thenReturn(멤버_ID);
        when(another.getId()).thenReturn(다른_멤버_ID);

        when(challengeParticipationRepository.findById(participationId))
                .thenReturn(Optional.of(챌린지_참여(another, ChallengeFixtures.챌린지(), 챌린지_멤버_역할, 챌린지_참여_대기_상태)));

        /* when */
        /* then */
        assertThrows(ForbiddenParticipationException.class, () -> challengeParticipationService.cancel(member, participationId));
    }
    
    @Test
    @DisplayName("챌린지 참여 신청 취소 시 챌린지 참여 상태가 대기 중이 아니라면 예외를 던진다.")
    void 챌린지_참여_신청_취소_시_챌린지_참여_상태가_대기_중이_아니라면_예외를_던진다 () {
        /* given */
        Long participationId = 챌린지_참여_ID;
        User member = spy(멤버);

        when(member.getId()).thenReturn(멤버_ID);

        when(challengeParticipationRepository.findById(participationId))
                .thenReturn(Optional.of(챌린지_참여(member, ChallengeFixtures.챌린지(), 챌린지_멤버_역할, 챌린지_참여_수락_상태)));

        /* when */
        /* then */
        assertThrows(InvalidParticipationStatusActionException.class, () -> challengeParticipationService.cancel(member, participationId));
    }

    private void verifyChallengeParticipationResponse(ChallengeParticipation participation, ChallengeParticipationResponse.Detail response) {
        assertEquals(participation.getRole(), response.role());
        assertEquals(participation.getStatus(), response.status());
        assertEquals(participation.getCreatedAt(), response.createdAt());
    }

    private void verifyChallengeResponseSummary(Challenge challenge, ChallengeResponse.Summary response) {
        assertEquals(challenge.getId(), response.id());
        assertEquals(challenge.getTitle(), response.title());
        assertEquals(challenge.getDescription(), response.description());
        assertEquals(challenge.getStatus(), response.status());
        assertEquals(challenge.getCategory(), response.category());
    }

    private void verifyUserResponse(User user, UserResponse response) {
        assertEquals(user.getId(), response.id());
        assertEquals(user.getUserId(), response.userId());
        assertEquals(user.getEmail(), response.email());
        assertEquals(user.getNickname(), response.nickname());
        assertEquals(user.getGender(), response.gender());
        assertEquals(user.getBirth(), response.birth());
    }
}