package com.trybe.modulecore.chat.enums;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // enum 이게 필요한가? enum 잘 모르겠다.
public enum MessageType {
    ENTER,
    TALK,
    EXIT,
    SYSTEM
}
