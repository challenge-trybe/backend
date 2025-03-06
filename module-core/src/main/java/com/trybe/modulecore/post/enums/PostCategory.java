package com.trybe.modulecore.post.enums;

public enum PostCategory {
    QNA("질문/답변"),
    PROMOTION("홍보"),
    ETC("기타");

    private final String description;


    PostCategory(String description) {
        this.description = description;
    }
}
