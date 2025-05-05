package com.trybe.modulecore.challenge.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ChallengeCategory;
import com.trybe.modulecore.challenge.enums.ChallengeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.trybe.modulecore.challenge.entity.QChallenge.challenge;
import static com.trybe.modulecore.utils.QueryUtils.getSort;
import static com.trybe.modulecore.utils.QueryUtils.nullSafeBuilder;

@Repository
public class ChallengeCustomRepositoryImpl implements ChallengeCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public ChallengeCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Page<Challenge> getFilteredChallenges(String keyword, List<ChallengeStatus> statuses, List<ChallengeCategory> categories, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(titleOrDescriptionContains(keyword))
                .and(statusIn(statuses))
                .and(categoryIn(categories));

        List<Challenge> challenges = jpaQueryFactory
                .selectFrom(challenge)
                .where(builder)
                .orderBy(getSort(pageable, challenge))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(challenge.count())
                .from(challenge)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(challenges, pageable, total == null ? 0L : total);
    }

    @Override
    public List<Challenge> getRecommendedChallenges(List<ChallengeCategory> categories, List<String> keywords, int limit) {
        return jpaQueryFactory.select(challenge)
                .from(challenge)
                .where(categoryIn(categories).or(titleContains(keywords)))
                .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                .limit(limit)
                .fetch();
    }

    private BooleanBuilder titleContains(List<String> keywords) {
        BooleanBuilder builder = new BooleanBuilder();
        keywords.forEach(keyword -> builder.or(titleContains(keyword)));
        return builder;
    }

    private BooleanBuilder titleOrDescriptionContains(String keyword) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.or(titleContains(keyword)).or(descriptionContains(keyword));
        return builder;
    }

    private BooleanBuilder categoryIn(List<ChallengeCategory> categories) {
        return nullSafeBuilder(() -> challenge.category.in(categories));
    }

    private BooleanBuilder statusIn(List<ChallengeStatus> statuses) {
        return nullSafeBuilder(() -> challenge.status.in(statuses));
    }

    private BooleanBuilder titleContains(String keyword) {
        return nullSafeBuilder(() -> challenge.title.containsIgnoreCase(keyword));
    }

    private BooleanBuilder descriptionContains(String keyword) {
        return nullSafeBuilder(() -> challenge.description.containsIgnoreCase(keyword));
    }
}