package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.exception.InvalidChallengeStatusException;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.challenge.exception.participation.InvalidChallengeRoleActionException;
import com.trybe.moduleapi.chat.service.ChatService;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.challenge.enums.ChallengeRole;
import com.trybe.modulecore.challenge.enums.ChallengeStatus;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkCache;
import com.trybe.modulecore.challenge.repository.preference.ChallengePreferenceCache;
import com.trybe.modulecore.challenge.repository.view.ChallengeViewCache;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChallengeBookmarkCache challengeBookmarkCache;
    private final ChallengePreferenceCache challengePreferenceCache;
    private final ChallengeViewCache challengeViewCache;
    private final PopularChallengeService popularChallengeService;
    private final ChallengeRecommendationClientService challengeRecommendationClientService;
    private final ChatService chatService;

    public ChallengeService(ChallengeRepository challengeRepository, ChallengeParticipationRepository challengeParticipationRepository, ChallengeBookmarkCache challengeBookmarkCache, ChallengePreferenceCache challengePreferenceCache, ChallengeViewCache challengeViewCache, PopularChallengeService popularChallengeService, ChallengeRecommendationClientService challengeRecommendationClientService, ChatService chatService) {
        this.challengeRepository = challengeRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.challengeBookmarkCache = challengeBookmarkCache;
        this.challengePreferenceCache = challengePreferenceCache;
        this.challengeViewCache = challengeViewCache;
        this.popularChallengeService = popularChallengeService;
        this.challengeRecommendationClientService = challengeRecommendationClientService;
        this.chatService = chatService;
    }

    private static final int VIEW_POPULAR_SCORE = 1;
    private static final int POPULAR_CHALLENGE_COUNT = 20;

    @Transactional
    public ChallengeResponse.Detail save(User user, ChallengeRequest.Create request) {
        Challenge challenge = request.toEntity();
        Challenge savedChallenge = challengeRepository.save(challenge);

        ChallengeParticipation participation = new ChallengeParticipation(user, savedChallenge, ChallengeRole.LEADER, ParticipationStatus.ACCEPTED);
        challengeParticipationRepository.save(participation);
        challengePreferenceCache.addPreference(user.getId(), savedChallenge);
        chatService.create(savedChallenge);

        ChallengeResponse.Bookmark bookmark = new ChallengeResponse.Bookmark(0, false);
        return ChallengeResponse.Detail.from(savedChallenge, 1, bookmark);
    }

    @Transactional(readOnly = true)
    public ChallengeResponse.Detail find(User user, Long id) {
        Challenge challenge = getChallenge(id);

        if (user != null) {
            handleView(user.getId(), challenge.getId());
        }

        return createDetail(user, challenge);
    }

    @Transactional(readOnly = true)
    public PageResponse<ChallengeResponse.Preview> findAll(User user, ChallengeRequest.Read request, Pageable pageable) {
        Page<Challenge> challenges = challengeRepository.findAllByStatusInAndCategoryIn(request.statuses(), request.categories(), pageable);

        Page<ChallengeResponse.Preview> challengeSummaries = challenges.map(challenge -> createPreview(user, challenge));

        return new PageResponse<>(challengeSummaries);
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponse.Preview> getPopular(User user) {
        List<Challenge> challenges = popularChallengeService.getTopPopularChallenges(POPULAR_CHALLENGE_COUNT);

        return challenges.stream()
                .map(challenge -> createPreview(user, challenge))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponse.Preview> getRecommendations(User user) {
        return challengeRecommendationClientService.getChallengeRecommendations(user.getId());
    }

    @Transactional
    public ChallengeResponse.Detail updateContent(User user, Long id, ChallengeRequest.UpdateContent request) {
        Challenge challenge = getChallenge(id);

        validateLeader(user.getId(), id, "리더만 챌린지 정보를 수정할 수 있습니다.");
        validateChallengeStatus(challenge, true, ChallengeStatus.PENDING, "진행 예정인 챌린지만 정보를 수정할 수 있습니다.");

        challenge.updateContent(request.title(), request.description(), request.startDate(), request.endDate(), request.capacity(), request.category());

        return createDetail(user, challenge);
    }

    @Transactional
    public ChallengeResponse.Detail updateProof(User user, Long id, ChallengeRequest.UpdateProof request) {
        Challenge challenge = getChallenge(id);

        validateLeader(user.getId(), id, "리더만 챌린지 인증 정보를 수정할 수 있습니다.");
        validateChallengeStatus(challenge, true, ChallengeStatus.PENDING, "진행 예정인 챌린지만 인증 정보를 수정할 수 있습니다.");

        challenge.updateProof(request.proofWay(), request.proofCount());

        return createDetail(user, challenge);
    }

    @Transactional
    public void delete(User user, Long id) {
        Challenge challenge = getChallenge(id);

        validateLeader(user.getId(), id, "리더만 챌린지를 삭제할 수 있습니다.");
        validateChallengeStatus(challenge, false, ChallengeStatus.ONGOING, "진행 중인 챌린지는 삭제할 수 없습니다.");

        challengeBookmarkCache.removeBookmarksByChallenge(id);
        challengeParticipationRepository.deleteAllByChallengeId(id);
        chatService.delete(id);
        challengeRepository.delete(challenge);
    }

    private ChallengeResponse.Detail createDetail(User user, Challenge challenge) {
        int participantCount = getParticipantCount(challenge.getId());
        ChallengeResponse.Bookmark bookmark = createBookmark(user, challenge.getId());
        return ChallengeResponse.Detail.from(challenge, participantCount, bookmark);
    }

    private ChallengeResponse.Preview createPreview(User user, Challenge challenge) {
        int participantCount = getParticipantCount(challenge.getId());
        ChallengeResponse.Bookmark bookmark = createBookmark(user, challenge.getId());
        return ChallengeResponse.Preview.from(challenge, participantCount, bookmark);
    }

    private ChallengeResponse.Bookmark createBookmark(User user, Long challengeId) {
        int bookmarkCount = challengeBookmarkCache.getBookmarkCount(challengeId);
        Boolean bookmarked = user == null ? null : challengeBookmarkCache.isBookmarked(user.getId(), challengeId);
        return new ChallengeResponse.Bookmark(bookmarkCount, bookmarked);
    }

    private Challenge getChallenge(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(NotFoundChallengeException::new);
    }

    private int getParticipantCount(Long challengeId) {
        return challengeParticipationRepository.countByChallengeIdAndStatus(challengeId, ParticipationStatus.ACCEPTED);
    }

    private void validateChallengeStatus(Challenge challenge, boolean shouldBe, ChallengeStatus status, String message) {
        if ((shouldBe && challenge.getStatus().isNot(status)) || (!shouldBe && challenge.getStatus().is(status))) {
            throw new InvalidChallengeStatusException(message);
        }
    }

    private void validateLeader(Long userId, Long challengeId, String message) {
        if (!challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(userId, challengeId, ChallengeRole.LEADER)) {
            throw new InvalidChallengeRoleActionException(message);
        }
    }

    private void handleView(Long userId, Long challengeId) {
        if (!challengeViewCache.hasViewed(userId, challengeId)) {
            challengeViewCache.recordView(userId, challengeId);
            popularChallengeService.increasePopularity(challengeId, VIEW_POPULAR_SCORE);
        }
    }
}
