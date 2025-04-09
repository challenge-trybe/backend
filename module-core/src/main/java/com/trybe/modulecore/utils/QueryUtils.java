package com.trybe.modulecore.utils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;

import java.util.function.Supplier;

public class QueryUtils {
    public static <T> BooleanExpression ifNotNull(T value, Supplier<BooleanExpression> expressionSupplier) {
        return value != null ? expressionSupplier.get() : null;
    }

    public static BooleanBuilder nullSafeBuilder(Supplier<BooleanExpression> f) {
        try {
            return new BooleanBuilder(f.get());
        } catch (NullPointerException e) {
            return new BooleanBuilder();
        }
    }
}
