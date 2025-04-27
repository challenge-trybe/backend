package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.dto.ChallengeResponseAssembler;
import com.trybe.moduleapi.challenge.event.ChallengeEvent;
import com.trybe.moduleapi.challenge.event.ChallengeEventType;
import com.trybe.moduleapi.challenge.event.pub.ChallengeEventPublisher;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkCache;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChallengeBookmarkService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeBookmarkCache challengeBookmarkCache;
    private final ChallengeEventPublisher challengeEventPublisher;
    private final ChallengeResponseAssembler challengeResponseAssembler;

    public ChallengeBookmarkService(ChallengeRepository challengeRepository, ChallengeBookmarkCache challengeBookmarkCache, ChallengeEventPublisher challengeEventPublisher, ChallengeResponseAssembler challengeResponseAssembler) {
        this.challengeRepository = challengeRepository;
        this.challengeBookmarkCache = challengeBookmarkCache;
        this.challengeEventPublisher = challengeEventPublisher;
        this.challengeResponseAssembler = challengeResponseAssembler;
    }

    @Transactional
    public ChallengeResponse.Bookmark addBookmark(User user, Long challengeId) {
        Challenge challenge = getChallenge(challengeId);
        Long userId = user.getId();

        int count = challengeBookmarkCache.getBookmarkCount(challengeId);

        if (!challengeBookmarkCache.isBookmarked(userId, challengeId)) {
            challengeBookmarkCache.addBookmark(userId, challengeId);
            challengeEventPublisher.publish(new ChallengeEvent(challenge, userId, ChallengeEventType.BOOKMARK_ADD));
            count++;
        }

        return new ChallengeResponse.Bookmark(count, true);
    }

    @Transactional
    public ChallengeResponse.Bookmark removeBookmark(User user, Long challengeId) {
        Challenge challenge = getChallenge(challengeId);
        Long userId = user.getId();

        int count = challengeBookmarkCache.getBookmarkCount(challengeId);

        if (challengeBookmarkCache.isBookmarked(userId, challengeId)) {
            challengeBookmarkCache.removeBookmark(userId, challengeId);
            challengeEventPublisher.publish(new ChallengeEvent(challenge, userId, ChallengeEventType.BOOKMARK_REMOVE));
            count--;
        }

        return new ChallengeResponse.Bookmark(count, false);
    }

    @Transactional(readOnly = true)
    public PageResponse<ChallengeResponse.Preview> getMyBookmarkedChallenges(User user, Pageable pageable) {
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = start + pageable.getPageSize() - 1;

        Set<Long> challengeIds = challengeBookmarkCache.getBookmarkedChallenges(user.getId(), start, end);

        List<Challenge> challenges = challengeIds.isEmpty()
                ? Collections.emptyList()
                : challengeRepository.findAllByIdIn(challengeIds);

        List<ChallengeResponse.Preview> challengePreviews = sortChallenges(challenges, challengeIds).stream()
                .map(challenge -> challengeResponseAssembler.toPreview(challenge, user.getId()))
                .toList();

        int totalElements = challengeBookmarkCache.getUserBookmarkCount(user.getId());
        Page<ChallengeResponse.Preview> challengePage = new PageImpl<>(challengePreviews, pageable, totalElements);

        return new PageResponse<>(challengePage);
    }

    private List<Challenge> sortChallenges(List<Challenge> challenges, Set<Long> challengeIds) {
        Map<Long, Challenge> challengeMap = challenges.stream()
                .collect(Collectors.toMap(Challenge::getId, challenge -> challenge));

        return challengeIds.stream()
                .map(challengeMap::get)
                .toList();
    }

    private Challenge getChallenge(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(NotFoundChallengeException::new);
    }
}