package com.trybe.modulecore.user.enums;

import lombok.Getter;

@Getter
public enum Role {
    ROLE_USER("회원"),
    ROLE_ADMIN("관리자");
    public String description;

    Role(String description) {
        this.description = description;
    }
}
