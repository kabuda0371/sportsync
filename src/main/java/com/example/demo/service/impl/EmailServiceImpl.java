package com.example.demo.service.impl;

import com.example.demo.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Email Verification Code - SportSync");
            helper.setText(buildVerificationEmailHtml(code), true);
            mailSender.send(mimeMessage);
            log.info("验证码邮件发送成功，收件人: {}", toEmail);
        } catch (Exception e) {
            log.error("验证码邮件发送失败，收件人: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email, please try again later!");
        }
    }

    private String buildVerificationEmailHtml(String code) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <style>
                    @media (prefers-color-scheme: dark) {
                      .email-wrapper { background-color: #0d1117 !important; }
                      .email-card    { background-color: #0f172a !important; box-shadow: 0 4px 24px rgba(239,68,68,0.25) !important; }
                      .email-body    { background-color: #0f172a !important; }
                      .email-title   { color: #ffffff !important; }
                      .email-sub     { color: #cbd5e1 !important; }
                      .email-codebox { background-color: rgba(239,68,68,0.12) !important; border-color: #ef4444 !important; }
                      .email-code    { color: #f87171 !important; }
                      .email-note    { color: #475569 !important; }
                      .email-footer  { background-color: #0d1117 !important; border-top-color: rgba(249,115,22,0.2) !important; }
                      .email-footer p { color: #475569 !important; }
                    }
                  </style>
                </head>
                <body style="margin:0;padding:0;background-color:#eff2f6;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" class="email-wrapper"
                         style="background-color:#eff2f6;padding:40px 0;">
                    <tr>
                      <td align="center">
                        <table width="480" cellpadding="0" cellspacing="0" class="email-card"
                               style="background-color:#ffffff;border-radius:12px;overflow:hidden;
                                      box-shadow:0 4px 24px rgba(239,68,68,0.12);">
                          <!-- Header -->
                          <tr>
                            <td style="background:linear-gradient(135deg,#ef4444,#f97316);
                                       padding:32px 40px;text-align:center;">
                              <table cellpadding="0" cellspacing="0" style="margin:0 auto;">
                                <tr>
                                  <td style="vertical-align:middle;padding-right:10px;">
                                    <table cellpadding="0" cellspacing="0">
                                      <tr>
                                        <td style="width:40px;height:40px;background:rgba(255,255,255,0.2);
                                                   border-radius:10px;text-align:center;vertical-align:middle;">
                                          <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
                                               stroke="white" stroke-width="2.5"
                                               stroke-linecap="round" stroke-linejoin="round"
                                               style="display:inline-block;vertical-align:middle;">
                                            <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
                                          </svg>
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                  <td style="vertical-align:middle;">
                                    <span style="color:#ffffff;font-size:22px;font-weight:700;
                                                 letter-spacing:0.5px;">SportSync</span>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <!-- Body -->
                          <tr>
                            <td class="email-body" style="padding:40px 40px 32px;background-color:#ffffff;">
                              <p class="email-title" style="margin:0 0 8px;color:#1e293b;font-size:18px;font-weight:600;">
                                Email Verification
                              </p>
                              <p class="email-sub" style="margin:0 0 28px;color:#64748b;font-size:14px;line-height:1.6;">
                                Use the code below to complete your verification.
                                It will expire in <strong>5 minutes</strong>.
                              </p>
                              <!-- Code box -->
                              <div class="email-codebox" style="background-color:#fff5f2;border:2px dashed #ef4444;
                                          border-radius:10px;padding:20px;text-align:center;
                                          margin-bottom:28px;">
                                <span class="email-code" style="font-size:36px;font-weight:700;letter-spacing:10px;
                                             color:#ef4444;">%s</span>
                              </div>
                              <p class="email-note" style="margin:0;color:#94a3b8;font-size:13px;line-height:1.6;">
                                If you did not request this code, you can safely ignore this email.
                                Someone else might have typed your email address by mistake.
                              </p>
                            </td>
                          </tr>
                          <!-- Footer -->
                          <tr>
                            <td class="email-footer" style="background-color:#eff2f6;padding:20px 40px;
                                       border-top:1px solid rgba(239,68,68,0.12);text-align:center;">
                              <p style="margin:0;color:#94a3b8;font-size:12px;">
                                &copy; 2026 SportSync. All rights reserved.
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(code);
    }
}
