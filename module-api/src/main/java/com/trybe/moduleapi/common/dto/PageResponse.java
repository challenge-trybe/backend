package com.trybe.moduleapi.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int totalPages,
        long totalElements,
        int size,
        int number,
        boolean last
) {
    public PageResponse(Page<T> page) {
        this(page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber(),
                page.isLast());
    }
}