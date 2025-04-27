package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.dto.ChallengeResponseAssembler;
import com.trybe.moduleapi.challenge.event.ChallengeEvent;
import com.trybe.moduleapi.challenge.event.ChallengeEventType;
import com.trybe.moduleapi.challenge.event.pub.ChallengeEventPublisher;
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
import com.trybe.modulecore.challenge.repository.view.ChallengeViewCache;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChallengeBookmarkCache challengeBookmarkCache;
    private final ChallengeViewCache challengeViewCache;
    private final ChallengeEventPublisher challengeEventPublisher;
    private final PopularChallengeService popularChallengeService;
    private final ChallengeRecommendationClientService challengeRecommendationClientService;
    private final ChatService chatService;
    private final ChallengeResponseAssembler challengeResponseAssembler;

    public ChallengeService(ChallengeRepository challengeRepository, ChallengeParticipationRepository challengeParticipationRepository, ChallengeBookmarkCache challengeBookmarkCache, ChallengeViewCache challengeViewCache, ChallengeEventPublisher challengeEventPublisher, PopularChallengeService popularChallengeService, ChallengeRecommendationClientService challengeRecommendationClientService, ChatService chatService, ChallengeResponseAssembler challengeResponseAssembler) {
        this.challengeRepository = challengeRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.challengeBookmarkCache = challengeBookmarkCache;
        this.challengeViewCache = challengeViewCache;
        this.challengeEventPublisher = challengeEventPublisher;
        this.popularChallengeService = popularChallengeService;
        this.challengeRecommendationClientService = challengeRecommendationClientService;
        this.chatService = chatService;
        this.challengeResponseAssembler = challengeResponseAssembler;
    }

    private static final int RECOMMEND_CHALLENGE_COUNT = 20;
    private static final int POPULAR_CHALLENGE_COUNT = 20;

    @Transactional
    public ChallengeResponse.Detail save(User user, ChallengeRequest.Create request) {
        Challenge challenge = request.toEntity();
        Challenge savedChallenge = challengeRepository.save(challenge);

        ChallengeParticipation participation = new ChallengeParticipation(user, savedChallenge, ChallengeRole.LEADER, ParticipationStatus.ACCEPTED);
        challengeParticipationRepository.save(participation);
        challengeEventPublisher.publish(new ChallengeEvent(savedChallenge, user.getId(), ChallengeEventType.CREATE));
        chatService.create(savedChallenge);

        ChallengeResponse.Bookmark bookmark = new ChallengeResponse.Bookmark(0, false);
        return ChallengeResponse.Detail.from(savedChallenge, 1, bookmark);
    }

    @Transactional(readOnly = true)
    public ChallengeResponse.Detail find(User user, Long id) {
        Challenge challenge = getChallenge(id);
        Long userId = user == null ? null : user.getId();

        handleView(userId, challenge);

        return challengeResponseAssembler.toDetail(challenge, userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<ChallengeResponse.Preview> findAll(User user, ChallengeRequest.Read request, Pageable pageable) {
        Page<Challenge> challenges = challengeRepository.getFilteredChallenges(request.keyword(), request.statuses(), request.categories(), pageable);
        Long userId = user == null ? null : user.getId();

        Page<ChallengeResponse.Preview> challengePreviews = challenges.map(challenge -> challengeResponseAssembler.toPreview(challenge, userId));

        return new PageResponse<>(challengePreviews);
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponse.Preview> getPopular(User user) {
        List<Challenge> challenges = popularChallengeService.getTopPopularChallenges(POPULAR_CHALLENGE_COUNT);
        Long userId = user == null ? null : user.getId();

        return challenges.stream()
                .map(challenge -> challengeResponseAssembler.toPreview(challenge, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponse.Preview> getRecommendations(User user) {
        List<Challenge> challenges = challengeRecommendationClientService.getChallengeRecommendations(user.getId(), RECOMMEND_CHALLENGE_COUNT);

        if (challenges.size() < RECOMMEND_CHALLENGE_COUNT) {
            Set<Challenge> challengeSet = new LinkedHashSet<>(challenges);

            List<Challenge> popularChallenges = popularChallengeService.getTopPopularChallenges(RECOMMEND_CHALLENGE_COUNT);
            challengeSet.addAll(popularChallenges);

            challenges = new ArrayList<>(challengeSet);
        }

        if (challenges.size() > RECOMMEND_CHALLENGE_COUNT) {
            challenges = challenges.subList(0, RECOMMEND_CHALLENGE_COUNT);
        }

        Collections.shuffle(challenges);

        return challenges.stream()
                .map(challenge -> challengeResponseAssembler.toPreview(challenge, user.getId()))
                .toList();
    }

    @Transactional
    public ChallengeResponse.Detail updateContent(User user, Long id, ChallengeRequest.UpdateContent request) {
        Challenge challenge = getChallenge(id);

        validateLeader(user.getId(), id, "리더만 챌린지 정보를 수정할 수 있습니다.");
        validateChallengeStatus(challenge, true, ChallengeStatus.PENDING, "진행 예정인 챌린지만 정보를 수정할 수 있습니다.");

        challenge.updateContent(request.title(), request.description(), request.startDate(), request.endDate(), request.capacity(), request.category());

        return challengeResponseAssembler.toDetail(challenge, user.getId());
    }

    @Transactional
    public ChallengeResponse.Detail updateProof(User user, Long id, ChallengeRequest.UpdateProof request) {
        Challenge challenge = getChallenge(id);

        validateLeader(user.getId(), id, "리더만 챌린지 인증 정보를 수정할 수 있습니다.");
        validateChallengeStatus(challenge, true, ChallengeStatus.PENDING, "진행 예정인 챌린지만 인증 정보를 수정할 수 있습니다.");

        challenge.updateProof(request.proofWay(), request.proofCount());

        return challengeResponseAssembler.toDetail(challenge, user.getId());
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

    private Challenge getChallenge(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(NotFoundChallengeException::new);
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

    private void handleView(Long userId, Challenge challenge) {
        Long challengeId = challenge.getId();
        if (userId != null && !challengeViewCache.hasViewed(userId, challengeId)) {
            challengeViewCache.recordView(userId, challengeId);
            challengeEventPublisher.publish(new ChallengeEvent(challenge, userId, ChallengeEventType.VIEW));
        }
    }
}
