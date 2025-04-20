package com.trybe.moduleapi.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateUtils {
    public static final int INITIALIZE_HOUR = 4;

    public static LocalDate getToday() {
        LocalDateTime now = LocalDateTime.now();
        return (now.getHour() < INITIALIZE_HOUR)
                ? now.toLocalDate().minusDays(1)
                : now.toLocalDate();
    }
}
