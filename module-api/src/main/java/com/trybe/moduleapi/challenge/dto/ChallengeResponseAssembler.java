package com.trybe.moduleapi.challenge.dto;

import com.trybe.moduleapi.file.dto.FileResponse;
import com.trybe.moduleapi.file.service.FileManager;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkCache;
import com.trybe.modulecore.file.entity.File;
import org.springframework.stereotype.Component;

@Component
public class ChallengeResponseAssembler {
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChallengeBookmarkCache challengeBookmarkCache;
    private final FileManager fileManager;

    public ChallengeResponseAssembler(ChallengeParticipationRepository challengeParticipationRepository, ChallengeBookmarkCache challengeBookmarkCache, FileManager fileManager) {
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.challengeBookmarkCache = challengeBookmarkCache;
        this.fileManager = fileManager;
    }

    public ChallengeResponse.Detail toInitialDetail(Challenge challenge) {
        FileResponse thumbnail = toFileResponse(challenge.getThumbnail());
        ChallengeResponse.Bookmark bookmark = new ChallengeResponse.Bookmark(0, false);

        return ChallengeResponse.Detail.from(challenge, thumbnail, 0, bookmark);
    }

    public ChallengeResponse.Detail toDetail(Challenge challenge, Long userId) {
        Long challengeId = challenge.getId();

        int participantCount = getParticipantCount(challengeId);
        ChallengeResponse.Bookmark bookmark = toBookmark(challengeId, userId);
        FileResponse thumbnail = toFileResponse(challenge.getThumbnail());

        return ChallengeResponse.Detail.from(challenge, thumbnail, participantCount, bookmark);
    }

    public ChallengeResponse.Preview toPreview(Challenge challenge, Long userId) {
        Long challengeId = challenge.getId();

        int participantCount = getParticipantCount(challengeId);
        ChallengeResponse.Bookmark bookmark = toBookmark(challengeId, userId);
        FileResponse thumbnail = toFileResponse(challenge.getThumbnail());

        return ChallengeResponse.Preview.from(challenge, thumbnail, participantCount, bookmark);
    }

    public ChallengeResponse.Summary toSummary(Challenge challenge) {
        return ChallengeResponse.Summary.from(challenge);
    }

    private int getParticipantCount(Long challengeId) {
        return challengeParticipationRepository.countByChallengeIdAndStatus(challengeId, ParticipationStatus.ACCEPTED);
    }

    private ChallengeResponse.Bookmark toBookmark(Long challengeId, Long userId) {
        int bookmarkCount = challengeBookmarkCache.getBookmarkCount(challengeId);
        Boolean bookmarked = userId == null ? null : challengeBookmarkCache.isBookmarked(userId, challengeId);
        return new ChallengeResponse.Bookmark(bookmarkCount, bookmarked);
    }

    private FileResponse toFileResponse(File file) {
        return file == null ? null : FileResponse.from(file.getOriginalName(), fileManager.getFileUrl(file.getFilePath()));
    }
}
