package com.example.demo.interceptor;

import com.example.demo.common.UserContext;
import com.example.demo.exception.BusinessException;
import com.example.demo.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头中获取 Token
        String token = request.getHeader("Authorization");
        
        // 2. 检查 Token 格式是否为 Bearer 开头
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去除 "Bearer "
        } else {
            log.warn("未提供 Token 或 Token 格式错误");
            throw new BusinessException("未授权，请先登录！");
        }

        // 3. 验证并解析 Token
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            // 4. 将 userId 存入 UserContext
            UserContext.setUserId(userId);
            return true; // 放行
        } catch (Exception e) {
            log.warn("Token 无效或已过期: {}", e.getMessage());
            throw new BusinessException("Token 无效或已过期，请重新登录！");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求处理完后，清除 ThreadLocal 中的数据，防止内存泄漏和串数据
        UserContext.clear();
    }
}
