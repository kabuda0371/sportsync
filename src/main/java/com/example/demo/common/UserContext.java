package com.example.demo.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.demo.exception.BusinessException;

/**
 * 用户上下文，通过 Spring Security 获取当前登录用户的 ID
 */
public class UserContext {

    // 废弃原有的 setUserId, 因为通过 Spring Security 过滤链设置 SecurityContextHolder
    @Deprecated
    public static void setUserId(Long userId) {
        // 不再需要手动维护 ThreadLocal 
    }

    public static Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(401, "用户未登录");
        }
        
        // 我们的 JwtAuthenticationFilter 会将 userId 原封不动地存为 principal
        // 若使用 UserDetails，也可能获取其 username 作为 userId (如果 username 配置的是 userId 的字符串)
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                throw new BusinessException("用户信息解析异常");
            }
        } else if (principal instanceof Long) {
            return (Long) principal;
        } else if (principal instanceof UserDetails) {
            // 如果使用标准 UserDetails，我们可以把 ID 存在 username 字段里
            return Long.parseLong(((UserDetails) principal).getUsername());
        }
        
        throw new BusinessException("无法识别的用户身份");
    }

    // 废弃清除, Spring Security 的 SecurityContextPersistenceFilter 自动会在处理完成后清理 Context
    @Deprecated
    public static void clear() {
        // 不再需要手动清除
    }
}
