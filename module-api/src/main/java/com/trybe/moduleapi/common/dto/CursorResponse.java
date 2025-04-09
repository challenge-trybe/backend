package com.trybe.moduleapi.common.dto;

import java.util.List;

public record CursorResponse<T>(
        List<T> content,
        Long nextCursor,
        int size,
        boolean hasNext
) {
    public static <T> CursorResponse<T> of(List<T> content, Long nextCursor,  int size, boolean hasNext) {
        return new CursorResponse<>(content, nextCursor, size, hasNext);
    }
}
