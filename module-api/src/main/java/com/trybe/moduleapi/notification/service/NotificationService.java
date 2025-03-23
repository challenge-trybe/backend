package com.trybe.moduleapi.notification.service;

import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.notification.dto.NotificationResponse;
import com.trybe.moduleapi.notification.exception.ForbiddenNotificationException;
import com.trybe.moduleapi.notification.exception.NotFoundNotificationException;
import com.trybe.modulecore.notification.entity.Notification;
import com.trybe.modulecore.notification.repository.NotificationRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(User user, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("createdAt")));
        Page<Notification> notifications = notificationRepository.findAllByUserId(user.getId(), pageRequest);
        return new PageResponse<>(notifications.map(NotificationResponse::from));
    }

    @Transactional
    public void markAsRead(User user, Long notificationId) {
        Notification notification = getNotification(notificationId);
        validateNotificationOwner(user.getId(), notification);
        notification.markAsRead();
    }

    @Transactional
    public void delete(User user, Long notificationId) {
        Notification notification = getNotification(notificationId);
        validateNotificationOwner(user.getId(), notification);
        notificationRepository.delete(notification);
    }

    private Notification getNotification(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(NotFoundNotificationException::new);
    }

    private void validateNotificationOwner(Long userId, Notification notification) {
        if (userId != notification.getUserId()) {
            throw new ForbiddenNotificationException();
        }
    }
}