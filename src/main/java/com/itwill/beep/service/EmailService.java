package com.itwill.beep.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.domain.UserAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final VerificationTokenService tokenService;
    private final UserAccountRepository userAccountRepository;

    public void sendVerificationToken(UserAccountEntity user) {
        String token = tokenService.createVerificationToken(user);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            String link = "http://localhost:8081/settings/security/registrationConfirm?token=" + token;
            String content = "<p>Dear User,</p>" +
                "<p>To confirm your email, please click the link below:</p>" +
                "<a href=\"" + link + "\">Confirm Email</a>" +
                "<p>Ignore this email if you do remember requesting it.</p>";

            try {
                helper.setTo(user.getUserEmail());
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            helper.setSubject("Confirm your email");
            helper.setText(content, true);
            int test = JavaMailSenderImpl.DEFAULT_PORT;
            javaMailSender.send(message);

            log.info("Verification email sent to {}", user.getUserEmail());
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
        }
    }

    public void sendPasswordResetEmail(String email) {
        UserAccountEntity userAccount = userAccountRepository.findByUserEmail(email).orElseThrow();
        if (userAccount == null) {
            log.error("No user found with email: {}", email);
            throw new UsernameNotFoundException("해당 이메일로 등록된 사용자를 찾을 수 없습니다.");
        }

        String resetToken = tokenService.createVerificationToken(userAccount);
        String resetLink = "http://localhost:8081/user/reset-password?token=" + resetToken;
        String content = "<p>Dear User,</p>" +
            "<p>To reset your password, please click the link below:</p>" +
            "<a href=\"" + resetLink + "\">Reset Password</a>" +
            "<p>If you did not request a password reset, please ignore this email.</p>";

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(userAccount.getUserEmail());
            helper.setSubject("Password Reset Request");
            helper.setText(content, true);
            javaMailSender.send(message);
            log.info("Password reset email sent to {}", userAccount.getUserEmail());
        } catch (MessagingException e) {
            log.error("Failed to send password reset email", e);
            throw new MailSendException("Failed to send password reset email", e);
        }
    }
}