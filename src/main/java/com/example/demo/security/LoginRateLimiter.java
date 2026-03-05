package com.example.demo.security;

import com.example.demo.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 登录防刷限流器（防爆破组件）
 * 集中管理登录失败计数、账户锁定，基于 Redis 实现分布式限流。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginRateLimiter {

    private final StringRedisTemplate stringRedisTemplate;

    private static final int MAX_FAILED_ATTEMPTS = 5; // 最大连续失败次数
    private static final long LOCK_TIME_DURATION_MINUTES = 15; // 锁定时间：15 分钟
    private static final long ATTEMPT_EXPIRE_HOURS = 24; // 错误记录过期时间：24 小时

    private static final String ATTEMPT_KEY_PREFIX = "login:attempt:";
    private static final String LOCK_KEY_PREFIX = "login:lock:";

    /**
     * 校验当前邮箱是否还在锁定状态，如果在则抛出异常阻断请求
     * @param email 尝试登录的邮箱
     */
    public void checkLockout(String email) {
        String lockKey = LOCK_KEY_PREFIX + email;
        Long expire = stringRedisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
        
        if (expire != null && expire > 0) {
            log.warn("防爆破拦截：源自邮箱 {} 的高频异常调用，账号已被锁定，剩余解锁时间约 {} 分钟", email, expire);
            throw new BusinessException("密码错误次数过多，账号已锁定，请 " + expire + " 分钟后再试！");
        }
    }

    /**
     * 防爆破核心：记录登录失败次数，超限则进行锁定预警
     * @param email 尝试登录的邮箱
     */
    public void recordFailedAttempt(String email) {
        String attemptKey = ATTEMPT_KEY_PREFIX + email;
        String lockKey = LOCK_KEY_PREFIX + email;

        // 递增失败次数，如果 key 不存在则初始化为 1 
        Long count = stringRedisTemplate.opsForValue().increment(attemptKey);
        
        // 如果是第一次错误，设置过期时间为 24 小时
        if (count != null && count == 1) {
            stringRedisTemplate.expire(attemptKey, ATTEMPT_EXPIRE_HOURS, TimeUnit.HOURS);
        }

        if (count != null && count >= MAX_FAILED_ATTEMPTS) {
            // 达到最大失败次数，锁定账号并设置锁定时间为 15 分钟
            stringRedisTemplate.opsForValue().set(lockKey, "locked", LOCK_TIME_DURATION_MINUTES, TimeUnit.MINUTES);
            // 锁定后清除当前的失败次数，避免解锁马上继续触发
            stringRedisTemplate.delete(attemptKey);
            
            log.error("！！！！安全告警！！！！：邮箱 {} 连续输入错误密码达 {} 次，触发系统防爆破自保，账号封控锁定！", email, count);
        } else {
            long remaining = MAX_FAILED_ATTEMPTS - (count == null ? 0 : count);
            log.warn("凭证校验失败：邮箱 {} 密码输入错误 {} 次，剩余允许重试次数 {}", email, count, remaining);
        }
    }

    /**
     * 密码校验成功时调用，清空之前的错误记录和锁定状态
     * @param email 成功登录的邮箱
     */
    public void clearLock(String email) {
        String attemptKey = ATTEMPT_KEY_PREFIX + email;
        String lockKey = LOCK_KEY_PREFIX + email;
        
        stringRedisTemplate.delete(attemptKey);
        stringRedisTemplate.delete(lockKey);
    }
}
