package com.example.demo.security;

import com.example.demo.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT 认证过滤器，用于解析 Token 并设置 Spring Security 上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 1. 从请求头中获取 Token
        String token = getJwtFromRequest(request);

        // 2. 验证 Token 是否有效
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            try {
                // 3. 从 Token 中获取 userId 和 role
                Long userId = jwtUtil.getUserIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                // 4. 将角色转换为 GrantedAuthority
                // Spring Security 默认期望角色以 ROLE_ 开头，比如 ROLE_STAFF
                String roleName = (role == null) ? "ROLE_MEMBER" : "ROLE_" + role.toUpperCase();
                List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));

                // 5. 构造 Authentication 对象 (将 userId 存为主体 Principal)
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(String.valueOf(userId), null, authorities);
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. 存入 SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                log.error("JWT Token 验证并在 Security Context 设置认证信息失败", e);
                // 可以在这里通过 response 输出错误信息，或者放行让后面的鉴权去拦截 401
            }
        }

        // 7. 放行
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
