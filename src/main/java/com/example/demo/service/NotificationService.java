package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Notification;
import com.example.demo.enums.NotificationTypeEnum;
import com.example.demo.vo.NotificationVO;

import java.util.List;

public interface NotificationService extends IService<Notification> {

    void sendNotification(Long userId, String type, Long relatedId, Long bookingId, String message);

    default void sendNotification(Long userId, Long bookingId, String message) {
        sendNotification(userId, NotificationTypeEnum.BOOKING.getValue(), bookingId, bookingId, message);
    }

    List<NotificationVO> getMyNotifications(Long userId);

    void markAsRead(Long userId, Long notificationId);
}
