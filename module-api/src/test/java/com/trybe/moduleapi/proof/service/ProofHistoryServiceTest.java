package com.trybe.moduleapi.proof.service;

import com.trybe.moduleapi.challenge.exception.participation.InvalidParticipationStatusActionException;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.proof.dto.request.ProofHistoryRequest;
import com.trybe.moduleapi.proof.dto.response.ProofHistoryResponse;
import com.trybe.moduleapi.proof.event.model.ProofHistoryEvent;
import com.trybe.moduleapi.proof.event.pub.ProofEventPublisher;
import com.trybe.moduleapi.proof.exception.InvalidProofDateException;
import com.trybe.moduleapi.proof.exception.NotFoundProofException;
import com.trybe.moduleapi.proof.exception.history.DuplicatedProofHistoryException;
import com.trybe.moduleapi.proof.exception.history.ForbiddenProofHistoryException;
import com.trybe.moduleapi.proof.exception.history.InvalidProofHistoryStatusException;
import com.trybe.moduleapi.proof.exception.history.NotFoundProofHistoryException;
import com.trybe.moduleapi.proof.fixtures.ProofFixtures;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.proof.entity.Proof;
import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.proof.repository.ProofHistoryRepository;
import com.trybe.modulecore.proof.repository.ProofRepository;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.trybe.moduleapi.proof.fixtures.ProofHistoryFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProofHistoryServiceTest {
    @InjectMocks
    private ProofHistoryService proofHistoryService;

    @Mock
    private ProofHistoryRepository proofHistoryRepository;

    @Mock
    private ProofRepository proofRepository;

    @Mock
    private ChallengeParticipationRepository challengeParticipationRepository;

    @Mock
    private ProofEventPublisher proofEventPublisher;

    @Test
    @DisplayName("인증 기록 생성 시 저장된 인증 기록 정보를 반환한다.")
    void 인증_기록_생성_시_저장된_인증_기록_정보를_반환한다 () {
        /* given */
        Long proofId = ProofFixtures.인증_ID;
        ProofHistoryRequest.Create request = 인증_기록_생성_요청;
        ProofHistory proofHistory = 대기_인증_기록;

        when(proofRepository.findById(proofId))
                .thenReturn(Optional.of(오늘_인증));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(true);
        when(proofHistoryRepository.existsByProofIdAndUserId(any(), any()))
                .thenReturn(false);
        when(proofHistoryRepository.save(any(ProofHistory.class)))
                .thenReturn(proofHistory);

        /* when */
        ProofHistoryResponse.Summary result = proofHistoryService.save(UserFixtures.회원, proofId, request);

        /* then */
        assertEquals(proofHistory.getId(), result.id());
        assertEquals(proofHistory.getContent(), result.content());
        assertEquals(proofHistory.getStatus(), result.status());
//        assertEquals(proofHistory.getCreatedAt(), result.createdAt());

        verify(proofEventPublisher, times(1)).publish(any(ProofHistoryEvent.class));
    }
    
    @Test
    @DisplayName("인증 기록 생성 시 존재하지 않는 인증 ID가 주어지면 예외를 던진다.")
    void 인증_기록_생성_시_존재하지_않는_인증_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long proofId = ProofFixtures.인증_ID;
        ProofHistoryRequest.Create request = 인증_기록_생성_요청;

        when(proofRepository.findById(proofId))
                .thenReturn(Optional.empty());
        
        /* when */
        /* then */
        assertThrows(NotFoundProofException.class, () -> proofHistoryService.save(UserFixtures.회원, proofId, request));
    }
    
    @Test
    @DisplayName("인증 기록 생성 시 챌린지 멤버가 아닌 경우 예외를 던진다.")
    void 인증_기록_생성_시_챌린지_멤버가_아닌_경우_예외를_던진다 () {
        /* given */
        Long proofId = ProofFixtures.인증_ID;
        ProofHistoryRequest.Create request = 인증_기록_생성_요청;

        when(proofRepository.findById(proofId))
                .thenReturn(Optional.of(오늘_인증));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(false);
        
        /* when */
        /* then */
        assertThrows(InvalidParticipationStatusActionException.class, () -> proofHistoryService.save(UserFixtures.회원, proofId, request));
    }
    
    @Test
    @DisplayName("인증 기록 생성 시 인증 날짜가 오늘이 아닌 경우 예외를 던진다.")
    void 인증_기록_생성_시_인증_날짜가_오늘이_아닌_경우_예외를_던진다 () {
        /* given */
        Long proofId = ProofFixtures.인증_ID;
        ProofHistoryRequest.Create request = 인증_기록_생성_요청;

        when(proofRepository.findById(proofId))
                .thenReturn(Optional.of(내일_인증));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(true);

        /* when */
        /* then */
        assertThrows(InvalidProofDateException.class, () -> proofHistoryService.save(UserFixtures.회원, proofId, request));
    }

    @Test
    @DisplayName("인증 기록 생성 시 인증에 대한 기록이 이미 존재하는 경우 예외를 던진다.")
    void 인증_기록_생성_시_인증에_대한_기록이_이미_존재하는_경우_예외를_던진다 () {
        /* given */
        Long proofId = ProofFixtures.인증_ID;
        ProofHistoryRequest.Create request = 인증_기록_생성_요청;

        when(proofRepository.findById(proofId))
                .thenReturn(Optional.of(오늘_인증));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(true);
        when(proofHistoryRepository.existsByProofIdAndUserId(any(), any()))
                .thenReturn(true);

        /* when */
        /* then */
        assertThrows(DuplicatedProofHistoryException.class, () -> proofHistoryService.save(UserFixtures.회원, proofId, request));
    }

    @Test
    @DisplayName("인증 기록 목록 조회 시 주어진 인증에 대한 인증 기록 목록을 반환한다.")
    void 인증_기록_목록_조회_시_주어진_인증에_대한_인증_기록_목록을_반환한다 () {
        /* given */
        Long proofId = ProofFixtures.인증_ID;

        when(proofRepository.findById(proofId))
                .thenReturn(Optional.of(오늘_인증));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(true);
        when(proofHistoryRepository.findAllByProofId(any(), any()))
                .thenReturn(인증_기록_목록_페이지);

        /* when */
        PageResponse<ProofHistoryResponse.Summary> result = proofHistoryService.findAll(UserFixtures.회원, proofId, 페이지_요청);

        /* then */
        assertEquals(인증_기록_목록_페이지_응답.content().size(), result.content().size());
        assertEquals(인증_기록_목록_페이지_응답.totalElements(), result.totalElements());
    }

    @Test
    @DisplayName("인증 기록 목록 조회 시 존재하지 않는 인증 ID가 주어지면 예외를 던진다.")
    void 인증_기록_목록_조회_시_존재하지_않는_인증_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long proofId = ProofFixtures.인증_ID;

        when(proofRepository.findById(proofId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundProofException.class, () -> proofHistoryService.findAll(UserFixtures.회원, proofId, 페이지_요청));
    }
    @Test
    @DisplayName("인증 기록 목록 조회 시 챌린지 멤버가 아닌 경우 예외를 던진다.")
    void 인증_기록_목록_조회_시_챌린지_멤버가_아닌_경우_예외를_던진다 () {
        /* given */
        Long proofId = ProofFixtures.인증_ID;

        when(proofRepository.findById(proofId))
                .thenReturn(Optional.of(오늘_인증));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(false);

        /* when */
        /* then */
        assertThrows(InvalidParticipationStatusActionException.class, () -> proofHistoryService.findAll(UserFixtures.회원, proofId, 페이지_요청));
    }

    @Test
    @DisplayName("인증 기록 수정 시 수정된 인증 기록 정보를 반환한다.")
    void 인증_기록_수정_시_수정된_인증_기록_정보를_반환한다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        ProofHistoryRequest.Update request = 인증_기록_수정_요청;

        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(대기_인증_기록));

        /* when */
        ProofHistoryResponse.Summary result = proofHistoryService.update(UserFixtures.회원, proofHistoryId, request);

        /* then */
        assertEquals(대기_인증_기록.getId(), result.id());
        assertEquals(request.content(), result.content());
        assertEquals(대기_인증_기록.getStatus(), result.status());
//        assertEquals(대기_인증_기록.getCreatedAt(), result.createdAt());
    }

    @Test
    @DisplayName("인증 기록 수정 시 존재하지 않는 인증 기록 ID가 주어지면 예외를 던진다.")
    void 인증_기록_수정_시_존재하지_않는_인증_기록_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        ProofHistoryRequest.Update request = 인증_기록_수정_요청;

        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundProofHistoryException.class, () -> proofHistoryService.update(UserFixtures.회원, proofHistoryId, request));
    }

    @Test
    @DisplayName("인증 기록 수정 시 인증 기록의 작성자가 아닌 경우 예외를 던진다.")
    void 인증_기록_수정_시_인증_기록의_작성자가_아닌_경우_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        Long userId = 1L;
        User user = spy(UserFixtures.회원);
        ProofHistoryRequest.Update request = 인증_기록_수정_요청;

        when(user.getId()).thenReturn(userId);
        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(대기_인증_기록));

        /* when */
        /* then */
        assertThrows(ForbiddenProofHistoryException.class, () -> proofHistoryService.update(user, proofHistoryId, request));
    }

    @Test
    @DisplayName("인증 기록 수정 시 인증 기록이 대기 상태가 아닌 경우 예외를 던진다.")
    void 인증_기록_수정_시_인증_기록이_대기_상태가_아닌_경우_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        ProofHistoryRequest.Update request = 인증_기록_수정_요청;

        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(성공_인증_기록));

        /* when */
        /* then */
        assertThrows(InvalidProofHistoryStatusException.class, () -> proofHistoryService.update(UserFixtures.회원, proofHistoryId, request));
    }

    @Test
    @DisplayName("인증 기록 삭제 시 인증 기록을 삭제한다.")
    void 인증_기록_삭제_시_인증_기록을_삭제한다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;

        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(대기_인증_기록));

        /* when */
        proofHistoryService.delete(UserFixtures.회원, proofHistoryId);

        /* then */
        verify(proofHistoryRepository, atLeastOnce()).delete(any(ProofHistory.class));
    }

    @Test
    @DisplayName("인증 기록 삭제 시 존재하지 않는 인증 기록 ID가 주어지면 예외를 던진다.")
    void 인증_기록_삭제_시_존재하지_않는_인증_기록_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;

        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundProofHistoryException.class, () -> proofHistoryService.delete(UserFixtures.회원, proofHistoryId));
    }

    @Test
    @DisplayName("인증 기록 삭제 시 인증 기록의 작성자가 아닌 경우 예외를 던진다.")
    void 인증_기록_삭제_시_인증_기록의_작성자가_아닌_경우_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        Long userId = 1L;
        User user = spy(UserFixtures.회원);

        when(user.getId()).thenReturn(userId);
        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(대기_인증_기록));

        /* when */
        /* then */
        assertThrows(ForbiddenProofHistoryException.class, () -> proofHistoryService.delete(user, proofHistoryId));
    }
}