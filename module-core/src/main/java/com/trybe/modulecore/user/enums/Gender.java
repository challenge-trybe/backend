package com.trybe.modulecore.user.enums;

import lombok.Getter;

@Getter
public enum Gender {
    MALE("남성"), FEMALE("여성"), NULL("선택안함");
    private String description;

    Gender(String description) {
        this.description = description;
    }
}
