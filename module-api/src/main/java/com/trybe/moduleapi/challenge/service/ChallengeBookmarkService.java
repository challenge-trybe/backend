package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
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
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChallengeBookmarkCache challengeBookmarkCache;

    public ChallengeBookmarkService(ChallengeRepository challengeRepository, ChallengeParticipationRepository challengeParticipationRepository, ChallengeBookmarkCache challengeBookmarkCache) {
        this.challengeRepository = challengeRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.challengeBookmarkCache = challengeBookmarkCache;
    }

    @Transactional
    public ChallengeResponse.Bookmark addBookmark(User user, Long challengeId) {
        validateExistChallenge(challengeId);

        int count = challengeBookmarkCache.getBookmarkCount(challengeId);

        if (!challengeBookmarkCache.isBookmarked(user.getId(), challengeId)) {
            challengeBookmarkCache.addBookmark(user.getId(), challengeId);
            count++;
        }

        return new ChallengeResponse.Bookmark(count, true);
    }

    @Transactional
    public ChallengeResponse.Bookmark removeBookmark(User user, Long challengeId) {
        validateExistChallenge(challengeId);

        int count = challengeBookmarkCache.getBookmarkCount(challengeId);

        if (challengeBookmarkCache.isBookmarked(user.getId(), challengeId)) {
            challengeBookmarkCache.removeBookmark(user.getId(), challengeId);
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

        List<ChallengeResponse.Preview> challengeSummaries = sortChallenges(challenges, challengeIds).stream()
                .map(challenge -> {
                    int participantCount = challengeParticipationRepository.countByChallengeIdAndStatus(challenge.getId(), ParticipationStatus.ACCEPTED);
                    int bookmarkCount = challengeBookmarkCache.getBookmarkCount(challenge.getId());

                    ChallengeResponse.Bookmark bookmark = new ChallengeResponse.Bookmark(bookmarkCount, true);
                    return ChallengeResponse.Preview.from(challenge, participantCount, bookmark);
                })
                .collect(Collectors.toList());

        int totalElements = challengeBookmarkCache.getUserBookmarkCount(user.getId());
        Page<ChallengeResponse.Preview> challengePage = new PageImpl<>(challengeSummaries, pageable, totalElements);

        return new PageResponse<>(challengePage);
    }

    private List<Challenge> sortChallenges(List<Challenge> challenges, Set<Long> challengeIds) {
        Map<Long, Challenge> challengeMap = challenges.stream()
                .collect(Collectors.toMap(Challenge::getId, challenge -> challenge));

        return challengeIds.stream()
                .map(challengeMap::get)
                .collect(Collectors.toList());
    }

    private void validateExistChallenge(Long challengeId) {
        if (!challengeRepository.existsById(challengeId)) {
            throw new NotFoundChallengeException();
        }
    }
}