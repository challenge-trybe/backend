package com.trybe.modulecore.challenge.repository;

import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ChallengeCategory;
import com.trybe.modulecore.challenge.enums.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    public List<Challenge> findAllByStatusInAndCategoryIn(List<ChallengeStatus> statuses, List<ChallengeCategory> categories);
    public List<Challenge> findAllByStatusAndStartDate(ChallengeStatus status, LocalDate startDate);
    public List<Challenge> findAllByStatusAndEndDate(ChallengeStatus status, LocalDate endDate);
}