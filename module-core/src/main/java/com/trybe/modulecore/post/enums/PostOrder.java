package com.trybe.modulecore.post.enums;

import com.querydsl.core.types.OrderSpecifier;
import com.trybe.modulecore.post.entity.QPost;
import lombok.Getter;

import static com.trybe.modulecore.post.entity.QPost.*;

@Getter
public enum PostOrder {
    LATEST("최신순") {
        @Override
        public OrderSpecifier<?> getOrderSpecifier() {
            return post.id.desc();
        }
    };

    private final String description;

    PostOrder(String description) {
        this.description = description;
    }

    public abstract OrderSpecifier<?> getOrderSpecifier();
}
