package com.example.demo.service;

/**
 * 邮件发送服务接口
 */
public interface EmailService {

    /**
     * 发送邮箱验证码
     * @param toEmail 收件人邮箱
     * @param code 6位数字验证码
     */
    void sendVerificationCode(String toEmail, String code);

    /**
     * 发送密码重置验证码
     * @param toEmail 收件人邮箱
     * @param code 6位数字验证码
     */
    void sendPasswordResetCode(String toEmail, String code);
}
