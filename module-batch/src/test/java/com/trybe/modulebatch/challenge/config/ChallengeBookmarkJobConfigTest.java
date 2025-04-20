package com.trybe.modulebatch.challenge.config;

import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkRedisCache;
import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@SpringBatchTest
@EnableBatchProcessing
class ChallengeBookmarkJobConfigTest {
    @Autowired
    @Qualifier("syncChallengeBookmarkJob")
    private Job challengeBookmarkJob;
    
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    @Autowired
    private ChallengeBookmarkRepository challengeBookmarkRepository;

    private static final Long USER_ID = 999L;
    private static final Long CHALLENGE_ID = 999L;
    private static final Long USER_COUNT = 0L;
    private static final Long CHALLENGE_COUNT = 0L;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(challengeBookmarkJob);

        String challengeBookmarkAddedKey = ChallengeBookmarkRedisCache.CHALLENGE_BOOKMARK_ADDED_KEY;
        String challengeBookmarkDeletedKey = ChallengeBookmarkRedisCache.CHALLENGE_BOOKMARK_DELETED_KEY;

        long userId = USER_ID;
        long challengeId = CHALLENGE_ID;

        for (long i = 0; i < CHALLENGE_COUNT; i++) {
            for (long j = 0; j < USER_COUNT; j++) {
                long challengeIdToAdd = challengeId + i;
                long userIdToAdd = userId + j;

                String addedKey = String.format(challengeBookmarkAddedKey, challengeIdToAdd);
                String deletedKey = String.format(challengeBookmarkDeletedKey, challengeIdToAdd);

                redisTemplate.opsForSet().add(addedKey, userIdToAdd);
                redisTemplate.opsForSet().add(deletedKey, userIdToAdd);
            }
        }
    }

    @AfterEach
    void tearDown() {
        for (long i = 0; i < CHALLENGE_COUNT; i++) {
            for (long j = 0; j < USER_COUNT; j++) {
                long userIdToDelete = USER_ID + j;
                long challengeIdToDelete = CHALLENGE_ID + i;

                challengeBookmarkRepository.deleteByChallengeIdAndUserId(challengeIdToDelete, userIdToDelete);
            }
        }
    }
    
    @Test
    @DisplayName("챌린지 북마크 배치 JOB 실행 테스트")
    void 챌린지_북마크_배치_JOB_실행_테스트 () throws Exception {
        /* given */
        /* when */
        JobExecution execution = jobLauncherTestUtils.launchJob();

        /* then */
        assertEquals(ExitStatus.COMPLETED, execution.getExitStatus());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        String addedKey = ChallengeBookmarkRedisCache.CHALLENGE_BOOKMARK_ADDED_KEY;
        String deletedKey = ChallengeBookmarkRedisCache.CHALLENGE_BOOKMARK_DELETED_KEY;

        for (long i = 0; i < CHALLENGE_COUNT; i++) {
            String keyToCheckAdded = String.format(addedKey, CHALLENGE_ID + i);
            String keyToCheckDeleted = String.format(deletedKey, CHALLENGE_ID + i);
            assertFalse(redisTemplate.hasKey(keyToCheckAdded));
            assertFalse(redisTemplate.hasKey(keyToCheckDeleted));
        }
    }
}