package com.trybe.modulecore.post.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.enums.PostCategory;
import com.trybe.modulecore.post.enums.PostOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.trybe.modulecore.post.entity.QPost.*;

public class PostCustomRepositoryImpl implements PostCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public PostCustomRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }



    @Override
    public Page<Post> findAllByKeywordAndCategories(String keyword, List<PostCategory> categories, PostOrder order, Pageable pageable) {
        List<Post> result = jpaQueryFactory.select(post)
                                           .from(post)
                                           .where(containsKeyword(keyword), filterCategories(categories))
                                           .offset(pageable.getPageNumber() * pageable.getPageSize())
                                           .orderBy(getOrder(order))
                                           .limit(pageable.getPageSize())
                                           .fetch();

        Long totalCount = getTotalCount(keyword, categories);
        return new PageImpl<>(result, pageable, totalCount);
    }

    public BooleanExpression containsKeyword(String keyword) {
        return QueryUtils.ifNotNull(keyword, () -> post.title.containsIgnoreCase(keyword)
                                                             .or(post.content.containsIgnoreCase(keyword)));
    }
    
    public BooleanExpression filterCategories(List<PostCategory> categories) {
        return post.category.in(categories);
    }

    public OrderSpecifier<?> getOrder(PostOrder order) {
        return order.getOrderSpecifier();
    }

    private Long getTotalCount(String keyword, List<PostCategory> categories){
        return jpaQueryFactory.select(post.count())
                                         .from(post)
                                         .where(containsKeyword(keyword), filterCategories(categories))
                                         .fetchOne();
    }
    

}
