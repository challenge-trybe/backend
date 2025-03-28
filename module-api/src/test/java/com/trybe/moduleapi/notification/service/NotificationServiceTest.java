package com.trybe.moduleapi.notification.service;

import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.notification.dto.NotificationResponse;
import com.trybe.moduleapi.notification.exception.ForbiddenNotificationException;
import com.trybe.moduleapi.notification.exception.NotFoundNotificationException;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.notification.entity.Notification;
import com.trybe.modulecore.notification.repository.NotificationRepository;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static com.trybe.moduleapi.notification.fixtures.NotificationFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("알림 저장 시 저장된 알림을 반환한다.")
    void 알림_저장_시_저장된_알림을_반환한다 () {
        /* given */
        Notification notification = 읽지_않은_알림;

        when(notificationRepository.save(notification))
                .thenReturn(notification);

        /* when */
        Notification result = notificationService.save(notification);

        /* then */
        assertEquals(notification, result);
    }

    @Test
    @DisplayName("나의 알림 목록 조회 시 알림 목록을 반환한다.")
    void 나의_알림_목록_조회_시_알림_목록을_반환한다 () {
        /* given */
        Long userId = UserFixtures.회원_PK;
        User user = spy(UserFixtures.회원);

        when(user.getId()).thenReturn(userId);
        when(notificationRepository.findAllByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(알림_페이지);

        /* when */
        PageResponse<NotificationResponse> result = notificationService.getNotifications(user, 페이지_요청);

        /* then */
        assertEquals(알림_페이지_응답.content().size(), result.content().size());
        assertEquals(알림_페이지_응답.totalElements(), result.totalElements());
    }

    @Test
    @DisplayName("알림 읽음 처리 시 알림을 읽음 처리한다.")
    void 알림_읽음_처리_시_알림을_읽음_처리한다 () {
        /* given */
        Long notificationId = 알림_ID;
        Notification notification = spy(읽지_않은_알림);

        Long userId = UserFixtures.회원_PK;
        User user = spy(UserFixtures.회원);

        when(user.getId()).thenReturn(userId);
        when(notification.getUserId()).thenReturn(userId);
        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        /* when */
        notificationService.markAsRead(user, notificationId);

        /* then */
        assertTrue(notification.isRead());
    }

    @Test
    @DisplayName("알림 읽음 처리 시 존재하지 않는 알림 ID가 주어지면 예외를 던진다.")
    void 알림_읽음_처리_시_존재하지_않는_알림_ID가_주어지면_예외를_던진다 () {
        /* given */
        Long notificationId = 알림_ID;

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundNotificationException.class, () -> notificationService.markAsRead(UserFixtures.회원, notificationId));
    }

    @Test
    @DisplayName("알림 읽음 처리 시 자신의 알림이 아닌 경우 예외를 던진다.")
    void 알림_읽음_처리_시_자신의_알림이_아닌_경우_예외를_던진다 () {
        /* given */
        Long notificationId = 알림_ID;
        Notification notification = spy(읽지_않은_알림);

        Long userId = UserFixtures.회원_PK;
        Long otherUserId = 2L;
        User user = spy(UserFixtures.회원);

        when(user.getId()).thenReturn(userId);
        when(notification.getUserId()).thenReturn(otherUserId);
        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        /* when */
        /* then */
        assertThrows(ForbiddenNotificationException.class, () -> notificationService.markAsRead(user, notificationId));
    }

    @Test
    @DisplayName("알림 삭제 시 알림을 삭제한다.")
    void 알림_삭제_시_알림을_삭제한다 () {
        /* given */
        Notification notification = spy(읽지_않은_알림);

        Long userId = UserFixtures.회원_PK;
        User user = spy(UserFixtures.회원);

        when(user.getId()).thenReturn(userId);
        when(notification.getUserId()).thenReturn(userId);
        when(notificationRepository.findById(알림_ID))
                .thenReturn(Optional.of(notification));

        /* when */
        notificationService.delete(user, 알림_ID);
        
        /* then */
        verify(notificationRepository, times(1)).delete(notification);
    }
    
    @Test
    @DisplayName("알림 삭제 시 존재하지 않는 알림 ID가 주어지면 예외를 던진다.")
    void 알림_삭제_시_존재하지_않는_알림_ID가_주어지면_예외를_던진다 () {
        /* given */
        Notification notification = spy(읽지_않은_알림);

        when(notificationRepository.findById(알림_ID))
                .thenReturn(Optional.empty());
        
        /* when */
        /* then */
        assertThrows(NotFoundNotificationException.class, () -> notificationService.delete(UserFixtures.회원, 알림_ID));
    }

    @Test
    @DisplayName("알림 삭제 시 자신의 알림이 아닌 경우 예외를 던진다.")
    void 알림_삭제_시_자신의_알림이_아닌_경우_예외를_던진다 () {
        /* given */
        Long notificationId = 알림_ID;
        Notification notification = spy(읽지_않은_알림);

        Long userId = UserFixtures.회원_PK;
        Long otherUserId = 2L;
        User user = spy(UserFixtures.회원);

        when(user.getId()).thenReturn(userId);
        when(notification.getUserId()).thenReturn(otherUserId);
        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        /* when */
        /* then */
        assertThrows(ForbiddenNotificationException.class, () -> notificationService.delete(user, notificationId));
    }
}