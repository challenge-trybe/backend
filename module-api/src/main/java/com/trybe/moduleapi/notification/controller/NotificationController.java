package com.trybe.moduleapi.notification.controller;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.notification.dto.NotificationResponse;
import com.trybe.moduleapi.notification.service.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/my")
    public PageResponse<NotificationResponse> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable
    ) {
        return notificationService.getNotifications(userDetails.getUser(), pageable);
    }

    @PutMapping("/{notificationId}/read")
    public void markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(userDetails.getUser(), notificationId);
    }

    @DeleteMapping("/{notificationId}")
    public void deleteNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long notificationId
    ) {
        notificationService.delete(userDetails.getUser(), notificationId);
    }
}
