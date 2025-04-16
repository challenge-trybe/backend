package com.trybe.modulecore.challenge.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ChallengeCategory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.trybe.modulecore.challenge.entity.QChallenge.challenge;
import static com.trybe.modulecore.utils.QueryUtils.nullSafeBuilder;

@Repository
public class ChallengeCustomRepositoryImpl implements ChallengeCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public ChallengeCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public List<Challenge> getByCategoriesOrKeywords(List<ChallengeCategory> categories, List<String> keywords, int limit) {
        return jpaQueryFactory.select(challenge)
                .from(challenge)
                .where(categoryInOrTitleContains(categories, keywords))
                .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                .limit(limit)
                .fetch();
    }

    private BooleanBuilder categoryInOrTitleContains(List<ChallengeCategory> categories, List<String> keywords) {
        return categoryIn(categories).or(titleContains(keywords));
    }

    private BooleanBuilder categoryIn(List<ChallengeCategory> categories) {
        return nullSafeBuilder(() -> challenge.category.in(categories));
    }

    private BooleanBuilder titleContains(List<String> keywords) {
        BooleanBuilder builder = new BooleanBuilder();
        keywords.forEach(keyword -> builder.or(challenge.title.containsIgnoreCase(keyword)));
        return builder;
    }
}