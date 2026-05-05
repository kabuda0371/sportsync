package com.example.demo.service.impl;

import com.example.demo.service.EmailService;
import jakarta.mail.internet.InternetAddress;
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
        sendEmail(
                toEmail,
                "Email Verification Code - SportSync",
                buildVerificationEmailPlain(code),
                buildVerificationEmailHtml(code),
                "verification"
        );
    }

    @Override
    public void sendPasswordResetCode(String toEmail, String code) {
        sendEmail(
                toEmail,
                "Password Reset Code - SportSync",
                buildPasswordResetEmailPlain(code),
                buildPasswordResetEmailHtml(code),
                "password reset"
        );
    }

    private void sendEmail(String toEmail, String subject, String plain, String html, String emailType) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(new InternetAddress(fromEmail, "SportSync"));
            helper.setTo(toEmail);
            helper.setSubject(subject);
            // multipart/alternative: plain-text first, HTML second
            helper.setText(plain, html);
            mailSender.send(mimeMessage);
            log.info("{} email sent successfully, recipient: {}", emailType, toEmail);
        } catch (Exception e) {
            log.error("Failed to send {} email, recipient: {}", emailType, toEmail, e);
            throw new RuntimeException("Failed to send email, please try again later!");
        }
    }

    // ── Plain-text alternatives ────────────────────────────────────────────────

    private String buildVerificationEmailPlain(String code) {
        return """
                SportSync — Email Verification

                Your verification code is: %s

                This code will expire in 5 minutes.

                If you did not request this code, you can safely ignore this email.

                © 2026 SportSync. All rights reserved.
                """.formatted(code);
    }

    private String buildPasswordResetEmailPlain(String code) {
        return """
                SportSync — Password Reset

                Your password reset code is: %s

                This code will expire in 5 minutes.

                If you did not request a password reset, you can safely ignore this email
                and your password will remain unchanged.

                © 2026 SportSync. All rights reserved.
                """.formatted(code);
    }

    // ── HTML templates ─────────────────────────────────────────────────────────

    private String buildVerificationEmailHtml(String code) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Email Verification</title>
                </head>
                <body style="margin:0;padding:0;background-color:#eff2f6;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0"
                         style="background-color:#eff2f6;padding:40px 0;">
                    <tr>
                      <td align="center">
                        <table width="480" cellpadding="0" cellspacing="0"
                               style="background-color:#ffffff;border-radius:12px;overflow:hidden;
                                      box-shadow:0 4px 24px rgba(239,68,68,0.12);">
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
                          <tr>
                            <td style="padding:40px 40px 32px;background-color:#ffffff;">
                              <p style="margin:0 0 8px;color:#1e293b;font-size:18px;font-weight:600;">
                                Email Verification
                              </p>
                              <p style="margin:0 0 28px;color:#64748b;font-size:14px;line-height:1.6;">
                                Use the code below to complete your verification.
                                It will expire in <strong>5 minutes</strong>.
                              </p>
                              <div style="background-color:#fff5f2;border:2px dashed #ef4444;
                                          border-radius:10px;padding:20px;text-align:center;
                                          margin-bottom:28px;">
                                <span style="font-size:36px;font-weight:700;letter-spacing:10px;
                                             color:#ef4444;">%s</span>
                              </div>
                              <p style="margin:0;color:#94a3b8;font-size:13px;line-height:1.6;">
                                If you did not request this code, you can safely ignore this email.
                                Someone else might have typed your email address by mistake.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="background-color:#eff2f6;padding:20px 40px;
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

    private String buildPasswordResetEmailHtml(String code) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Password Reset</title>
                </head>
                <body style="margin:0;padding:0;background-color:#eff2f6;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0"
                         style="background-color:#eff2f6;padding:40px 0;">
                    <tr>
                      <td align="center">
                        <table width="480" cellpadding="0" cellspacing="0"
                               style="background-color:#ffffff;border-radius:12px;overflow:hidden;
                                      box-shadow:0 4px 24px rgba(249,115,22,0.12);">
                          <tr>
                            <td style="background:linear-gradient(135deg,#f97316,#f59e0b);
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
                                            <path d="M12 17a2 2 0 0 0 2-2v-1a2 2 0 1 0-4 0v1a2 2 0 0 0 2 2z"/>
                                            <path d="M7 11V8a5 5 0 0 1 10 0v3"/>
                                            <rect x="4" y="11" width="16" height="10" rx="2"/>
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
                          <tr>
                            <td style="padding:40px 40px 32px;background-color:#ffffff;">
                              <p style="margin:0 0 8px;color:#1e293b;font-size:18px;font-weight:600;">
                                Password Reset
                              </p>
                              <p style="margin:0 0 28px;color:#64748b;font-size:14px;line-height:1.6;">
                                Use the code below to reset your SportSync password.
                                It will expire in <strong>5 minutes</strong>.
                              </p>
                              <div style="background-color:#fff7ed;border:2px dashed #f97316;
                                          border-radius:10px;padding:20px;text-align:center;
                                          margin-bottom:28px;">
                                <span style="font-size:36px;font-weight:700;letter-spacing:10px;
                                             color:#f97316;">%s</span>
                              </div>
                              <p style="margin:0;color:#94a3b8;font-size:13px;line-height:1.6;">
                                If you did not request a password reset, you can safely ignore this email
                                and your password will remain unchanged.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="background-color:#eff2f6;padding:20px 40px;
                                       border-top:1px solid rgba(249,115,22,0.12);text-align:center;">
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
