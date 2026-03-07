package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Notification;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.NotificationMapper;
import com.example.demo.service.NotificationService;
import com.example.demo.vo.NotificationVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(Long userId, Long bookingId, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .bookingId(bookingId)
                .message(message)
                .isRead(false)
                .build();
        this.save(notification);
    }

    @Override
    public List<NotificationVO> getMyNotifications(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt);

        return this.list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = this.getById(notificationId);
        if (notification == null) {
            throw new BusinessException(404, "Notification not found");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(403, "Unauthorized to operate on others' notifications");
        }
        notification.setIsRead(true);
        this.updateById(notification);
    }

    private NotificationVO convertToVO(Notification notification) {
        return NotificationVO.builder()
                .id(notification.getId())
                .bookingId(notification.getBookingId())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
