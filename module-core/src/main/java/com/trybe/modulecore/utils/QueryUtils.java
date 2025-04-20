package com.trybe.modulecore.utils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.data.domain.Pageable;

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

    public static <T> OrderSpecifier<?>[] getSort(Pageable pageable, EntityPathBase<T> qClass) {
        return pageable.getSort().stream()
                .map(order ->
                    new OrderSpecifier(
                            Order.valueOf(order.getDirection().name()),
                            Expressions.path(
                                    qClass.getType(),
                                    qClass,
                                    order.getProperty()
                            )
                    )
                )
                .toArray(OrderSpecifier[]::new);
    }
}
