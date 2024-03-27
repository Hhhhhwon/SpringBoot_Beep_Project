package com.itwill.beep.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.domain.VerificationToken;
import com.itwill.beep.domain.VerificationTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private final VerificationTokenRepository tokenRepository;

    public String createVerificationToken(UserAccountEntity user) {
        String token = UUID.randomUUID().toString();
        VerificationToken myToken = new VerificationToken(token, user);
        tokenRepository.save(myToken);
        return token;
    }

    public VerificationToken getVerificationToken(String VerificationToken) {
        return tokenRepository.findByVerifiTokenValue(VerificationToken);
    }

    // 토큰으로 검색
    public VerificationToken findByToken(String token) {
        return tokenRepository.findByVerifiTokenValue(token);
    }

    // 토큰 무효화
    public void invalidateToken(VerificationToken token) {
        tokenRepository.delete(token);
    }

    // 만료된 토큰 정리 - 배치 작업이나 스케줄러에 의해 주기적으로 호출될 수 있음
    public void cleanExpiredTokens() {
        // 만료된 토큰을 찾아 삭제하는 로직 구현
    }
    // ... 이메일 전송 및 토큰 검증 로직 ...
}