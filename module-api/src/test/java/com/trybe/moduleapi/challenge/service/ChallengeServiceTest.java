package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.exception.InvalidChallengeStatusException;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.challenge.exception.participation.InvalidChallengeRoleActionException;
import com.trybe.moduleapi.challenge.fixtures.ChallengeParticipationFixtures;
import com.trybe.moduleapi.chat.service.ChatService;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.challenge.enums.ChallengeRole;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkCache;
import com.trybe.modulecore.challenge.repository.preference.ChallengePreferenceCache;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {
    @InjectMocks
    private ChallengeService challengeService;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private ChallengeParticipationRepository challengeParticipationRepository;

    @Mock
    private ChallengeBookmarkCache challengeBookmarkCache;

    @Mock
    private ChallengePreferenceCache challengePreferenceCache;

    @Mock
    private ChallengeRecommendationClientService challengeRecommendationClientService;

    @Mock
    private ChatService chatService;

    @Test
    @DisplayName("챌린지 생성 시 저장된 챌린지 정보를 반환한다.")
    void 챌린지_생성_시_저장된_챌린지_정보를_반환한다 () {
        /* given */
        ChallengeRequest.Create request = 챌린지_생성_요청;
        Challenge 챌린지 = 챌린지();

        when(challengeRepository.save(any(Challenge.class)))
                .thenReturn(챌린지);
        when(challengeParticipationRepository.save(any(ChallengeParticipation.class)))
                .thenReturn(ChallengeParticipationFixtures.챌린지_리더_참여());

        /* when */
        ChallengeResponse.Detail response = challengeService.save(UserFixtures.회원, request);

        /* then */
        verifyChallengeResponse(챌린지, response);
        verify(chatService, times(1)).create(챌린지);
        assertEquals(초기_참여자_수, response.participantCount());
        assertEquals(초기_북마크_수, response.bookmark().bookmarkCount());
        assertEquals(false, response.bookmark().bookmarked());

        verify(challengePreferenceCache, times(1)).addPreference(any(), any(Challenge.class));
    }

    @Test
    @DisplayName("챌린지 단일 조회 시 챌린지 정보를 반환한다.")
    void 챌린지_단일_조회_시_챌린지_정보를_반환한다 () {
        /* given */
        Long challengeId = 챌린지_ID;
        Challenge challenge = 챌린지();

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(challenge));
        when(challengeParticipationRepository.countByChallengeIdAndStatus(any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(참여자_수);
        when(challengeBookmarkCache.getBookmarkCount(any()))
                .thenReturn(북마크_수);
        when(challengeBookmarkCache.isBookmarked(any(), any()))
                .thenReturn(false);

        /* when */
        ChallengeResponse.Detail response = challengeService.find(UserFixtures.회원, challengeId);

        /* then */
        verifyChallengeResponse(challenge, response);
        assertEquals(참여자_수, response.participantCount());
        assertEquals(북마크_수, response.bookmark().bookmarkCount());
        assertEquals(false, response.bookmark().bookmarked());
    }

    @Test
    @DisplayName("챌린지 단일 조회 시 로그아웃 상태인 경우 null 북마크 정보를 담은 챌린지 정보를 반환한다.")
    void 챌린지_단일_조회_시_로그아웃_상태인_경우_null_북마크_정보를_담은_챌린지_정보를_반환한다 () {
        /* given */
        Long challengeId = 챌린지_ID;
        Challenge challenge = 챌린지();

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(challenge));
        when(challengeParticipationRepository.countByChallengeIdAndStatus(any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(참여자_수);
        when(challengeBookmarkCache.getBookmarkCount(any()))
                .thenReturn(북마크_수);

        /* when */
        ChallengeResponse.Detail response = challengeService.find(null, challengeId);

        /* then */
        verifyChallengeResponse(challenge, response);
        assertEquals(참여자_수, response.participantCount());
        assertEquals(북마크_수, response.bookmark().bookmarkCount());
        assertEquals(null, response.bookmark().bookmarked());
    }

    @Test
    @DisplayName("챌린지 단일 조회 시 존재하지 않는 챌린지 ID가 주어지면 예외를 던진다.")
    void 챌린지_단일_조회_시_존재하지_않는_챌린지_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long challengeId = 챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeException.class, () -> challengeService.find(UserFixtures.회원, challengeId));
    }

    @Test
    @DisplayName("챌린지 목록 조회 시 요청에 따른 필터링된 챌린지 정보를 반환한다.")
    void 챌린지_목록_조회_시_요청에_따른_필터링된_챌린지_정보를_반환한다 () {
        /* given */
        ChallengeRequest.Read request = 챌린지_조회_요청;

        when(challengeRepository.findAllByStatusInAndCategoryIn(request.statuses(), request.categories(), 페이지_요청))
                .thenReturn(챌린지_페이지);
        when(challengeParticipationRepository.countByChallengeIdAndStatus(any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(참여자_수);
        when(challengeBookmarkCache.getBookmarkCount(any()))
                .thenReturn(북마크_수);
        when(challengeBookmarkCache.isBookmarked(any(), any()))
                .thenReturn(false);

        /* when */
        PageResponse<ChallengeResponse.Preview> response = challengeService.findAll(UserFixtures.회원, request, 페이지_요청);

        /* then */
        assertEquals(챌린지_페이지_응답.totalElements(), response.totalElements());
    }

    @Test
    @DisplayName("챌린지 추천 목록 조회 시 추천된 챌린지 정보를 반환한다.")
    void 챌린지_추천_목록_조회_시_추천된_챌린지_정보를_반환한다 () {
        /* given */
        User user = spy(UserFixtures.회원);
        Long userId = UserFixtures.회원_PK;

        when(user.getId()).thenReturn(userId);
        when(challengeRecommendationClientService.getChallengeRecommendations(any()))
                .thenReturn(챌린지_추천_목록_응답);

        /* when */
        List<ChallengeResponse.Preview> response = challengeService.getRecommendations(user);

        /* then */
        assertEquals(챌린지_추천_목록_응답.size(), response.size());
    }

    @Test
    @DisplayName("챌린지 정보 수정 시 수정된 챌린지 정보를 반환한다.")
    void 챌린지_정보_수정_시_수정된_챌린지_정보를_반환한다 () {
        /* given */
        Long challengeId = 챌린지_ID;
        ChallengeRequest.UpdateContent request = 챌린지_내용_수정_요청;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(챌린지()));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(any(), eq(challengeId), eq(ChallengeRole.LEADER)))
                .thenReturn(true);
        when(challengeParticipationRepository.countByChallengeIdAndStatus(any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(참여자_수);
        when(challengeBookmarkCache.getBookmarkCount(any()))
                .thenReturn(북마크_수);
        when(challengeBookmarkCache.isBookmarked(any(), any()))
                .thenReturn(false);

        /* when */
        ChallengeResponse.Detail response = challengeService.updateContent(UserFixtures.회원, challengeId, request);

        /* then */
        verifyChallengeResponse(내용_수정된_챌린지, response);
        assertEquals(참여자_수, response.participantCount());
        assertEquals(북마크_수, response.bookmark().bookmarkCount());
        assertEquals(false, response.bookmark().bookmarked());
    }

    @Test
    @DisplayName("챌린지 정보 수정 시 리더가 아닌 경우 예외를 던진다.")
    void 챌린지_정보_수정_시_리더가_아닌_경우_예외를_던진다 () {
        /* given */
        Long challengeId = 챌린지_ID;
        ChallengeRequest.UpdateContent request = 챌린지_내용_수정_요청;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(챌린지()));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(any(), eq(challengeId), eq(ChallengeRole.LEADER)))
                .thenReturn(false);

        /* when */
        /* then */
        assertThrows(InvalidChallengeRoleActionException.class, () -> challengeService.updateContent(UserFixtures.회원, challengeId, request));
    }

    @Test
    @DisplayName("챌린지 정보 수정 시 진행 예정 챌린지가 아닌 경우 예외를 던진다.")
    void 챌린지_정보_수정_시_진행_예정_챌린지가_아닌_경우_예외를_던진다 () {
        /* given */
        Long challengeId = 챌린지_ID;
        ChallengeRequest.UpdateContent request = 챌린지_내용_수정_요청;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(진행중인_챌린지));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(any(), eq(challengeId), eq(ChallengeRole.LEADER)))
                .thenReturn(true);

        /* when */
        /* then */
        assertThrows(InvalidChallengeStatusException.class, () -> challengeService.updateContent(UserFixtures.회원, challengeId, request));
    }

    @Test
    @DisplayName("챌린지 정보 수정 시 존재하지 않는 챌린지 ID가 주어지면 예외를 던진다.")
    void 챌린지_정보_수정_시_존재하지_않는_챌린지_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long challengeId = 챌린지_ID;
        ChallengeRequest.UpdateContent request = 챌린지_내용_수정_요청;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeException.class, () -> challengeService.updateContent(UserFixtures.회원, challengeId, request));
    }

    @Test
    @DisplayName("챌린지 인증 정보 수정 시 수정된 챌린지 정보를 반환한다.")
    void 챌린지_인증_정보_수정_시_수정된_챌린지_정보를_반환한다 () {
        /* given */
        Long challengeId = 챌린지_ID;
        ChallengeRequest.UpdateProof request = 챌린지_인증_내용_수정_요청;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(챌린지()));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(UserFixtures.회원.getId(), challengeId, ChallengeRole.LEADER))
                .thenReturn(true);
        when(challengeParticipationRepository.countByChallengeIdAndStatus(any(), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(참여자_수);
        when(challengeBookmarkCache.getBookmarkCount(any()))
                .thenReturn(북마크_수);
        when(challengeBookmarkCache.isBookmarked(any(), any()))
                .thenReturn(false);

        /* when */
        ChallengeResponse.Detail response = challengeService.updateProof(UserFixtures.회원, challengeId, request);

        /* then */
        verifyChallengeResponse(인증_내용_수정된_챌린지, response);
        assertEquals(참여자_수, response.participantCount());
        assertEquals(북마크_수, response.bookmark().bookmarkCount());
        assertEquals(false, response.bookmark().bookmarked());
    }

    @Test
    @DisplayName("챌린지 인증 정보 수정 시 리더가 아닌 경우 예외를 던진다.")
    void 챌린지_인증_정보_수정_시_리더가_아닌_경우_예외를_던진다 () {
        /* given */
        Long challengeId = 챌린지_ID;
        ChallengeRequest.UpdateProof request = 챌린지_인증_내용_수정_요청;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(챌린지()));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(UserFixtures.회원.getId(), challengeId, ChallengeRole.LEADER))
                .thenReturn(false);

        /* when */
        /* then */
        assertThrows(InvalidChallengeRoleActionException.class, () -> challengeService.updateProof(UserFixtures.회원, challengeId, request));
    }

    @Test
    @DisplayName("챌린지 인증 정보 수정 시 진행 예정 챌린지가 아닌 경우 예외를 던진다")
    void 챌린지_인증_정보_수정_시_진행_예정_챌린지가_아닌_경우_예외를_던진다 () {
        /* given */
        Long challengeId = 챌린지_ID;
        ChallengeRequest.UpdateProof request = 챌린지_인증_내용_수정_요청;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(진행중인_챌린지));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(UserFixtures.회원.getId(), challengeId, ChallengeRole.LEADER))
                .thenReturn(true);

        /* when */
        /* then */
        assertThrows(InvalidChallengeStatusException.class, () -> challengeService.updateProof(UserFixtures.회원, challengeId, request));
    }

    @Test
    @DisplayName("챌린지 인증 정보 수정 시 존재하지 않는 챌린지 ID가 주어지면 예외를 던진다.")
    void 챌린지_인증_정보_수정_시_존재하지_않는_챌린지_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long challengeId = 챌린지_ID;
        ChallengeRequest.UpdateProof request = 챌린지_인증_내용_수정_요청;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeException.class, () -> challengeService.updateProof(UserFixtures.회원, challengeId, request));
    }

    @Test
    @DisplayName("챌린지 삭제 시 챌린지를 삭제한다.")
    void 챌린지_삭제_시_챌린지를_삭제한다 () {
        /* given */
        Long challengeId = 챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(챌린지()));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(UserFixtures.회원.getId(), challengeId, ChallengeRole.LEADER))
                .thenReturn(true);

        doNothing().when(challengeRepository).delete(any(Challenge.class));
        doNothing().when(chatService).delete(challengeId);
        doNothing().when(challengeParticipationRepository).deleteAllByChallengeId(challengeId);
        doNothing().when(challengeBookmarkCache).removeBookmarksByChallenge(challengeId);

        /* when */
        /* then */
        challengeService.delete(UserFixtures.회원, challengeId);

        verify(challengeRepository, atLeastOnce()).delete(any(Challenge.class));
        verify(chatService, atLeastOnce()).delete(challengeId);
        verify(challengeParticipationRepository, atLeastOnce()).deleteAllByChallengeId(challengeId);
        verify(challengeBookmarkCache, atLeastOnce()).removeBookmarksByChallenge(challengeId);
    }

    @Test
    @DisplayName("챌린지 삭제 시 리더가 아닌 경우 예외를 던진다.")
    void 챌린지_삭제_시_리더가_아닌_경우_예외를_던진다 () {
        /* given */
        Long challengeId = 챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(챌린지()));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(UserFixtures.회원.getId(), challengeId, ChallengeRole.LEADER))
                .thenReturn(false);

        /* when */
        /* then */
        assertThrows(InvalidChallengeRoleActionException.class, () -> challengeService.delete(UserFixtures.회원, challengeId));
    }

    @Test
    @DisplayName("챌린지 삭제 시 진행 중인 챌린지인 경우 예외를 던진다.")
    void 챌린지_삭제_시_진행_중인_챌린지인_경우_예외를_던진다 () {
        /* given */
        Long challengeId = 챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.of(진행중인_챌린지));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(UserFixtures.회원.getId(), challengeId, ChallengeRole.LEADER))
                .thenReturn(true);

        /* when */
        /* then */
        assertThrows(InvalidChallengeStatusException.class, () -> challengeService.delete(UserFixtures.회원, challengeId));
    }

    @Test
    @DisplayName("챌린지 삭제 시 존재하지 않는 챌린지 ID가 주어지면 예외를 던진다.")
    void 챌린지_삭제_시_존재하지_않는_챌린지_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long challengeId = 챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeException.class, () -> challengeService.delete(UserFixtures.회원, challengeId));
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
