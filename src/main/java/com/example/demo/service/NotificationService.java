package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Notification;
import com.example.demo.vo.NotificationVO;

import java.util.List;

public interface NotificationService extends IService<Notification> {

    /**
     * 发送一条站内通知给指定用户
     *
     * @param userId    接收通知的用户ID
     * @param bookingId 关联的预订ID
     * @param message   通知内容
     */
    void sendNotification(Long userId, Long bookingId, String message);

    /**
     * 获取指定用户的所有通知（按创建时间倒序）
     */
    List<NotificationVO> getMyNotifications(Long userId);

    /**
     * 将指定通知标记为已读
     *
     * @param userId         当前操作用户（防止越权）
     * @param notificationId 要标记的通知ID
     */
    void markAsRead(Long userId, Long notificationId);
}
