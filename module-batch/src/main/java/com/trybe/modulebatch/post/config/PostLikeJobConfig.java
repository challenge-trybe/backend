package com.trybe.modulebatch.post.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PostLikeJobConfig {
    public static final int CHUNK_SIZE = 100;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final ItemReader postLikeInsertReader;
    private final ItemWriter postLikeInsertWriter;

    private final ItemReader postLikeDeleteReader;
    private final ItemWriter postLikeDeleteWriter;

    public PostLikeJobConfig(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
                                  @Qualifier("postLikeInsertReader") ItemReader postLikeInsertReader, @Qualifier("postLikeInsertWriter") ItemWriter postLikeInsertWriter,
                                  @Qualifier("postLikeDeleteReader")ItemReader postLikeDeleteReader, @Qualifier("postLikeDeleteWriter") ItemWriter postLikeDeleteWriter) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.postLikeInsertReader = postLikeInsertReader;
        this.postLikeInsertWriter = postLikeInsertWriter;
        this.postLikeDeleteReader = postLikeDeleteReader;
        this.postLikeDeleteWriter = postLikeDeleteWriter;
    }

    @Bean
    public Job postLikeBulkUpdateJob() {
        return new JobBuilder("postLikeJob", jobRepository)
                .start(postLikeInsertStep())
                .next(postLikeDeleteStep())
                .build();
    }

    @Bean
    public Step postLikeInsertStep() {
        return new StepBuilder("postLikeInsertStep", jobRepository)
                .chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(postLikeInsertReader)
                .writer(postLikeInsertWriter)
                .build();
    }

    @Bean
    public Step postLikeDeleteStep() {
        return new StepBuilder("postLikeDeleteStep", jobRepository)
                .chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(postLikeDeleteReader)
                .writer(postLikeDeleteWriter)
                .build();
    }
}
