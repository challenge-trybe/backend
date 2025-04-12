package com.trybe.modulebatch.config;

import com.trybe.modulecore.post.repository.PostLikeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@SpringBatchTest
@EnableBatchProcessing
class PostLikeBatchJobConfigTest {
    @Autowired
    private Job postLikeJob;
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Test
    public void 게시글_좋아요_배치_JOB_실행확인() throws Exception {
        //given
        insertPostLikeTestData();
        String uniqueParameter = String.valueOf(System.currentTimeMillis());
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        JobParameters jobParameters = jobParametersBuilder
                .addString("chunkSize", "100")
                .addString("uniqueKey", uniqueParameter)
                .toJobParameters();

        //when
        jobLauncherTestUtils.setJob(postLikeJob);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
    }

    private void insertPostLikeTestData() {
        for (long postId = 1; postId <= 3; postId++) {
            String postKey = String.format("post:%d:liked", postId);
            String deleteKey = postKey + ":deleted";

            Set<Long> userIds = new HashSet<>();
            for (long userId = 1; userId <= 5; userId++) {
                userIds.add(userId);
            }

            redisTemplate.opsForSet().add(postKey, userIds.toArray(new Long[0]));

            Set<Long> deleteUserIds = new HashSet<>(Arrays.asList(1L, 3L));
            redisTemplate.opsForSet().add(deleteKey, deleteUserIds.toArray(new Long[0]));
        }
    }
}
