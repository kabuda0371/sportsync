package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.common.UserContext;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.NotificationService;
import com.example.demo.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "通知管理", description = "查询会员站内通知，支持标记已读")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/my")
    @Operation(summary = "查询我的通知", description = "获取当前登录会员的所有站内通知，按时间倒序排列")
    public Result<List<NotificationVO>> getMyNotifications() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        return Result.success(notificationService.getMyNotifications(userId));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读状态")
    public Result<Void> markAsRead(
            @Parameter(description = "通知ID") @PathVariable Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        notificationService.markAsRead(userId, id);
        return Result.success(null);
    }
}
