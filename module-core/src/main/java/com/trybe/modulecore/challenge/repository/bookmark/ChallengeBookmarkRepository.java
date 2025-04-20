package com.trybe.modulecore.challenge.repository.bookmark;

import com.trybe.modulecore.challenge.entity.ChallengeBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeBookmarkRepository extends JpaRepository<ChallengeBookmark, Long>, ChallengeBookmarkCustomRepository {
    void deleteByChallengeIdAndUserId(Long challengeId, Long userId);
}
