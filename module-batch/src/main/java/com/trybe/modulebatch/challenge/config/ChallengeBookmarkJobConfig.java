package com.trybe.modulebatch.challenge.config;

import com.trybe.modulebatch.challenge.job.dto.ChallengeBookmarkDto;
import com.trybe.modulecore.challenge.entity.ChallengeBookmark;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ChallengeBookmarkJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final ItemReader<ChallengeBookmarkDto> challengeBookmarkInsertReader;
    private final ItemReader<ChallengeBookmarkDto> challengeBookmarkDeleteReader;

    private final ItemProcessor<ChallengeBookmarkDto, ChallengeBookmark> challengeBookmarkProcessor;

    private final ItemWriter<ChallengeBookmark> challengeBookmarkInsertWriter;
    private final ItemWriter<ChallengeBookmark> challengeBookmarkDeleteWriter;

    private final ItemReader<String> deleteChallengeBookmarkRedisKeyReader;
    private final ItemWriter<String> deleteChallengeBookmarkRedisKeyWriter;

    public ChallengeBookmarkJobConfig(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("challengeBookmarkInsertReader") ItemReader<ChallengeBookmarkDto> challengeBookmarkInsertReader,
            @Qualifier("challengeBookmarkDeleteReader") ItemReader<ChallengeBookmarkDto> challengeBookmarkDeleteReader,
            @Qualifier("challengeBookmarkProcessor") ItemProcessor<ChallengeBookmarkDto, ChallengeBookmark> challengeBookmarkProcessor,
            @Qualifier("challengeBookmarkInsertWriter") ItemWriter<ChallengeBookmark> challengeBookmarkInsertWriter,
            @Qualifier("challengeBookmarkDeleteWriter") ItemWriter<ChallengeBookmark> challengeBookmarkDeleteWriter,
            @Qualifier("deleteChallengeBookmarkRedisKeyReader") ItemReader<String> deleteChallengeBookmarkRedisKeyReader,
            @Qualifier("deleteChallengeBookmarkRedisKeyWriter") ItemWriter<String> deleteChallengeBookmarkRedisKeyWriter
    ) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.challengeBookmarkInsertReader = challengeBookmarkInsertReader;
        this.challengeBookmarkDeleteReader = challengeBookmarkDeleteReader;
        this.challengeBookmarkProcessor = challengeBookmarkProcessor;
        this.challengeBookmarkInsertWriter = challengeBookmarkInsertWriter;
        this.challengeBookmarkDeleteWriter = challengeBookmarkDeleteWriter;
        this.deleteChallengeBookmarkRedisKeyReader = deleteChallengeBookmarkRedisKeyReader;
        this.deleteChallengeBookmarkRedisKeyWriter = deleteChallengeBookmarkRedisKeyWriter;
    }

    private static final int CHUNK_SIZE = 100;

    private static final String JOB_NAME = "syncChallengeBookmarkJob";
    private static final String INSERT_STEP_NAME = "insertChallengeBookmarkStep";
    private static final String DELETE_STEP_NAME = "deleteChallengeBookmarkStep";
    private static final String DELETE_REDIS_STEP_NAME = "deleteChallengeBookmarkRedisStep";

    @Bean
    public Job syncChallengeBookmarkJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(insertChallengeBookmarkStep())
                .next(deleteChallengeBookmarkStep())
                .next(deleteChallengeBookmarkRedisKeyStep())
                .build();
    }

    @Bean
    public Step insertChallengeBookmarkStep() {
        return new StepBuilder(INSERT_STEP_NAME, jobRepository)
                .<ChallengeBookmarkDto, ChallengeBookmark>chunk(CHUNK_SIZE, transactionManager)
                .reader(challengeBookmarkInsertReader)
                .processor(challengeBookmarkProcessor)
                .writer(challengeBookmarkInsertWriter)
                .build();
    }

    @Bean
    public Step deleteChallengeBookmarkStep() {
        return new StepBuilder(DELETE_STEP_NAME, jobRepository)
                .<ChallengeBookmarkDto, ChallengeBookmark>chunk(CHUNK_SIZE, transactionManager)
                .reader(challengeBookmarkDeleteReader)
                .processor(challengeBookmarkProcessor)
                .writer(challengeBookmarkDeleteWriter)
                .build();
    }

    @Bean
    public Step deleteChallengeBookmarkRedisKeyStep() {
        return new StepBuilder(DELETE_REDIS_STEP_NAME, jobRepository)
                .<String, String>chunk(CHUNK_SIZE, transactionManager)
                .reader(deleteChallengeBookmarkRedisKeyReader)
                .writer(deleteChallengeBookmarkRedisKeyWriter)
                .build();
    }
}
