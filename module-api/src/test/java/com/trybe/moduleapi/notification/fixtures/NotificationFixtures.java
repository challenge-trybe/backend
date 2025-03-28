package com.trybe.moduleapi.notification.fixtures;

import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.notification.dto.NotificationResponse;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.notification.entity.Notification;
import com.trybe.modulecore.notification.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationFixtures {
    public static final Long 알림_ID = 1L;
    public static final NotificationType 알림_타입 = NotificationType.CHALLENGE;
    public static final Long 알림_타입_ID = ChallengeFixtures.챌린지_ID;
    public static final String 알림_제목 = "챌린지가 시작되었습니다!";
    public static final String 알림_내용 = "\"클린코드 도서 읽기\" 챌린지가 시작되었습니다. 챌린지에 참여해보세요!";
    public static final LocalDateTime 알림_생성_시간 = LocalDateTime.of(2025, 3, 28, 3, 22, 48);

    /* Entity */
    private Notification 알림_생성(boolean isRead) {
        Notification notification = new Notification(UserFixtures.회원_PK, 알림_타입, 알림_타입_ID, 알림_제목, 알림_내용);
        if (isRead) notification.markAsRead();
        return notification;
    }

    public static final Notification 읽지_않은_알림 = new NotificationFixtures().알림_생성(false);
    public static final Notification 읽은_알림 = new NotificationFixtures().알림_생성(true);
    public static final List<Notification> 알림_목록 = List.of(읽지_않은_알림, 읽은_알림);
    public static final Pageable 페이지_요청 = PageRequest.of(0, 10);
    public static final Page<Notification> 알림_페이지 = new PageImpl<>(알림_목록, 페이지_요청, 알림_목록.size());

    /* Response DTO */
    public static final NotificationResponse 읽지_않은_알림_응답 = 알림_응답_생성(읽지_않은_알림);
    public static final NotificationResponse 읽은_알림_응답 = 알림_응답_생성(읽은_알림);
    private static Page<NotificationResponse> 알림_응답_페이지 = new PageImpl<>(List.of(읽지_않은_알림_응답, 읽은_알림_응답), 페이지_요청, 2);
    public static final PageResponse<NotificationResponse> 알림_페이지_응답 = new PageResponse<>(알림_응답_페이지);

    private static NotificationResponse 알림_응답_생성(Notification notification) {
        return new NotificationResponse(알림_ID, notification.getType(), notification.getTypeId(), notification.getTitle(), notification.getMessage(), 알림_생성_시간, notification.isRead());
    }
}