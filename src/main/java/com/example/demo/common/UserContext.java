package com.example.demo.common;

/**
 * 用户上下文，用于在当前线程中保存登录用户的 ID
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID_THREAD_LOCAL = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID_THREAD_LOCAL.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_THREAD_LOCAL.get();
    }

    public static void clear() {
        USER_ID_THREAD_LOCAL.remove();
    }
}
