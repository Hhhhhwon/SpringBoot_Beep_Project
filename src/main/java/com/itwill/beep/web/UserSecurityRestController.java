package com.itwill.beep.web;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.domain.UserAccountRepository;
import com.itwill.beep.domain.VerificationToken;
import com.itwill.beep.service.EmailService;
import com.itwill.beep.service.VerificationTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/settings/security")
@RequiredArgsConstructor
public class UserSecurityRestController {

    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final UserAccountRepository userAccountRepository;

    // 이메일 인증 요청 처리
    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@AuthenticationPrincipal UserDetails userdetails) {
        if (userdetails != null) {
            UserAccountEntity user = userAccountRepository.findByUserName(userdetails.getUsername());
            emailService.sendVerificationToken(user);
            return ResponseEntity.ok("Verification email sent.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated.");
        }
    }

    // 이메일 인증 확인 처리
    @GetMapping("/registrationConfirm")
    public ResponseEntity<String> confirmRegistration(@RequestParam("token") String token) {
        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
        if (verificationToken == null) {
            return ResponseEntity.badRequest().body("Invalid token.");
        }

        UserAccountEntity user = verificationToken.getVerifiTokenUserAccount();
        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid token.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (verificationToken.getVerifiTokenExpiryDate().isBefore(now)) {
            return ResponseEntity.badRequest().body("Token expired.");
        }

        user.updateEmailVerified(true); // 이메일 인증 여부를 true로 업데이트
        userAccountRepository.save(user); // 변경된 상태를 저장

        return ResponseEntity.ok("Email verified successfully.");
    }
}