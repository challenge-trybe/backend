package com.trybe.moduleapi.proof.service;

import com.trybe.moduleapi.challenge.exception.participation.InvalidParticipationStatusActionException;
import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.proof.dto.response.ProofHistoryVoteResponse;
import com.trybe.moduleapi.proof.exception.history.DuplicatedProofHistoryVoteException;
import com.trybe.moduleapi.proof.exception.history.ForbiddenProofHistoryException;
import com.trybe.moduleapi.proof.exception.history.InvalidProofHistoryStatusException;
import com.trybe.moduleapi.proof.exception.history.NotFoundProofHistoryException;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.proof.repository.ProofHistoryRepository;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;

import static com.trybe.moduleapi.proof.fixtures.ProofHistoryFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProofHistoryVoteServiceTest {
    @InjectMocks
    private ProofHistoryVoteService proofHistoryVoteService;

    @Mock
    private ProofHistoryRepository proofHistoryRepository;

    @Mock
    private ChallengeParticipationRepository challengeParticipationRepository;

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForHash()).thenReturn(mock());
        lenient().when(redisTemplate.opsForValue()).thenReturn(mock());
    }

    @Test
    @DisplayName("인증 기록 투표 생성 시 투표 결과를 반환한다.")
    void 인증_기록_투표_생성_시_투표_결과를_반환한다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        boolean approved = true;
        Long userId = UserFixtures.회원_PK;
        User user = spy(UserFixtures.회원);

        when(user.getId()).thenReturn(userId);
        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(대기_인증_기록));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(true);
        when(redisTemplate.opsForHash().get(any(String.class), any(String.class)))
                .thenReturn(null);
        when(redisTemplate.opsForValue().increment(any(String.class), anyLong()))
                .thenReturn(1L);

        /* when */
        ProofHistoryVoteResponse.My result = proofHistoryVoteService.save(user, proofHistoryId, approved);

        /* then */
        verify(redisTemplate.opsForValue(), atLeastOnce()).increment(any(String.class), anyLong());
        verify(redisTemplate.opsForHash(), atLeastOnce()).put(any(String.class), any(String.class), any(String.class));
        assertEquals(approved, result.approved());
    }

    @Test
    @DisplayName("인증 기록 투표 생성 시 존재하지 않는 인증 기록 ID가 주어지면 예외를 던진다.")
    void 인증_기록_투표_생성_시_존재하지_않는_인증_기록_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        boolean approved = true;

        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundProofHistoryException.class, () -> proofHistoryVoteService.save(UserFixtures.회원, proofHistoryId, approved));
    }

    @Test
    @DisplayName("인증 기록 투표 생성 시 챌린지 멤버가 아닌 경우 예외를 던진다.")
    void 인증_기록_투표_생성_시_챌린지_멤버가_아닌_경우_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        boolean approved = true;

        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(대기_인증_기록));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(false);

        /* when */
        /* then */
        assertThrows(InvalidParticipationStatusActionException.class, () -> proofHistoryVoteService.save(UserFixtures.회원, proofHistoryId, approved));
    }

    @Test
    @DisplayName("인증 기록 투표 생성 시 자신의 인증 기록에 투표하는 경우 예외를 던진다.")
    void 인증_기록_투표_생성_시_자신의_인증_기록에_투표하는_경우_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        boolean approved = true;

        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(대기_인증_기록));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(true);

        /* when */
        /* then */
        assertThrows(ForbiddenProofHistoryException.class, () -> proofHistoryVoteService.save(UserFixtures.회원, proofHistoryId, approved));
    }

    @Test
    @DisplayName("인증 기록 투표 생성 시 이미 처리된 인증 기록에 투표하는 경우 예외를 던진다.")
    void 인증_기록_투표_생성_시_이미_처리된_인증_기록에_투표하는_경우_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        Long userId = UserFixtures.회원_PK;
        User user = spy(UserFixtures.회원);
        boolean approved = true;

        when(user.getId()).thenReturn(userId);
        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(성공_인증_기록));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(true);

        /* when */
        /* then */
        assertThrows(InvalidProofHistoryStatusException.class, () -> proofHistoryVoteService.save(user, proofHistoryId, approved));
    }

    @Test
    @DisplayName("인증 기록 투표 생성 시 인증 기록에 이미 투표한 경우 예외를 던진다.")
    void 인증_기록_투표_생성_시_인증_기록에_이미_투표한_경우_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        Long userId = UserFixtures.회원_PK;
        User user = spy(UserFixtures.회원);
        boolean approved = true;
        String approvedValue = approved ? "approved" : "disapproved";

        when(user.getId()).thenReturn(userId);
        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(대기_인증_기록));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(true);
        when(redisTemplate.opsForHash().get(any(String.class), any(String.class)))
                .thenReturn(approvedValue);

        /* when */
        /* then */
        assertThrows(DuplicatedProofHistoryVoteException.class, () -> proofHistoryVoteService.save(user, proofHistoryId, approved));
    }
    
    @Test
    @DisplayName("인증 기록에 대한 나의 투표 이력 조회 시 투표 이력을 반환한다.")
    void 인증_기록에_대한_나의_투표_이력_조회_시_투표_이력을_반환한다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        Boolean approved = true;
        String approvedValue = approved ? "approved" : "disapproved";

        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(대기_인증_기록));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(true);
        when(redisTemplate.opsForHash().get(any(String.class), any(String.class)))
                .thenReturn(approvedValue);

        /* when */
        ProofHistoryVoteResponse.My result = proofHistoryVoteService.findMyVote(UserFixtures.회원, proofHistoryId);

        /* then */
        assertEquals(approved, result.approved());
    }
    
    @Test
    @DisplayName("인증 기록에 대한 나의 투표 이력 조회 시 존재하지 않는 인증 기록 ID가 주어지면 예외를 던진다.")
    void 인증_기록에_대한_나의_투표_이력_조회_시_존재하지_않는_인증_기록_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;

        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.empty());
        
        /* when */
        /* then */
        assertThrows(NotFoundProofHistoryException.class, () -> proofHistoryVoteService.findMyVote(UserFixtures.회원, proofHistoryId));
    }
    
    @Test
    @DisplayName("인증 기록에 대한 나의 투표 이력 조회 시 챌린지 멤버가 아닌 경우 예외를 던진다.")
    void 인증_기록에_대한_나의_투표_이력_조회_시_챌린지_멤버가_아닌_경우_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        
        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(대기_인증_기록));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(false);
        
        /* when */
        /* then */
        assertThrows(InvalidParticipationStatusActionException.class, () -> proofHistoryVoteService.findMyVote(UserFixtures.회원, proofHistoryId));
    }
    
    @Test
    @DisplayName("인증 기록에 대한 나의 투표 이력 조회 시 이미 처리된 인증 기록에 대한 조회인 경우 예외를 던진다.")
    void 인증_기록에_대한_나의_투표_이력_조회_시_이미_처리된_인증_기록에_대한_조회인_경우_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        
        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(성공_인증_기록));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(true);
        
        /* when */
        /* then */
        assertThrows(InvalidProofHistoryStatusException.class, () -> proofHistoryVoteService.findMyVote(UserFixtures.회원, proofHistoryId));
    }
    
    @Test
    @DisplayName("인증 기록에 대한 투표 결과 조회 시 투표 결과를 반환한다.")
    void 인증_기록에_대한_투표_결과_조회_시_투표_결과를_반환한다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        ProofHistory proofHistory = spy(대기_인증_기록);
        Long approvedCount = 1L;
        Long disapprovedCount = 1L;

        String approvedCountKey = String.format("proofHistory:%d:votes:approvedCount", proofHistoryId);
        String disapprovedCountKey = String.format("proofHistory:%d:votes:disapprovedCount", proofHistoryId);

        when(proofHistory.getId()).thenReturn(proofHistoryId);
        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(proofHistory));
        when(redisTemplate.opsForValue().get(approvedCountKey))
                .thenReturn(approvedCount);
        when(redisTemplate.opsForValue().get(disapprovedCountKey))
                .thenReturn(disapprovedCount);
        when(challengeParticipationRepository.countByChallengeIdAndStatus(any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(ChallengeFixtures.참여자_수);

        /* when */
        ProofHistoryVoteResponse.Result result = proofHistoryVoteService.getResult(UserFixtures.회원, proofHistoryId);
        
        /* then */
        int nonParticipatedCount = (ChallengeFixtures.참여자_수 - 1) - (approvedCount.intValue() + disapprovedCount.intValue());

        assertEquals(approvedCount.intValue(), result.approvedCount());
        assertEquals(disapprovedCount.intValue(), result.disapprovedCount());
        assertEquals(nonParticipatedCount, result.nonParticipatedCount());
    }

    @Test
    @DisplayName("인증 기록에 대한 투표 결과 조회 시 존재하지 않는 인증 기록 ID가 주어지면 예외를 던진다.")
    void 인증_기록에_대한_투표_결과_조회_시_존재하지_않는_인증_기록_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;

        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundProofHistoryException.class, () -> proofHistoryVoteService.getResult(UserFixtures.회원, proofHistoryId));
    }

    @Test
    @DisplayName("인증 기록에 대한 투표 결과 조회 시 인증 기록의 작성자가 아닌 경우 예외를 던진다.")
    void 인증_기록에_대한_투표_결과_조회_시_인증_기록의_작성자가_아닌_경우_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;
        Long userId = UserFixtures.회원_PK;
        User user = spy(UserFixtures.회원);

        when(user.getId()).thenReturn(userId);
        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(대기_인증_기록));

        /* when */
        /* then */
        assertThrows(ForbiddenProofHistoryException.class, () -> proofHistoryVoteService.getResult(user, proofHistoryId));
    }

    @Test
    @DisplayName("인증 기록에 대한 투표 결과 조회 시 이미 처리된 인증 기록에 대한 조회인 경우 예외를 던진다.")
    void 인증_기록에_대한_투표_결과_조회_시_이미_처리된_인증_기록에_대한_조회인_경우_예외를_던진다 () {
        /* given */
        Long proofHistoryId = 인증_기록_ID;

        when(proofHistoryRepository.findById(proofHistoryId))
                .thenReturn(Optional.of(성공_인증_기록));

        /* when */
        /* then */
        assertThrows(InvalidProofHistoryStatusException.class, () -> proofHistoryVoteService.getResult(UserFixtures.회원, proofHistoryId));
    }
}
