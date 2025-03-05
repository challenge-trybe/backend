package com.trybe.modulecore.post.repository;

import com.querydsl.core.types.dsl.BooleanExpression;

import java.util.function.Supplier;

public class QueryUtils {
    public static <T> BooleanExpression ifNotNull(T value, Supplier<BooleanExpression> expressionSupplier) {
        return value != null ? expressionSupplier.get() : null;
    }
}
