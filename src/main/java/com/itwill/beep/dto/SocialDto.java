package com.itwill.beep.dto;

import java.security.SecureRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.itwill.beep.domain.UserAccountEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@Getter
@Setter
@NoArgsConstructor
public class SocialDto {

    private String userEmail;
    private String userName;
    private String userNickname;
    private String userPassword;

    @Builder
    public SocialDto(String userEmail, String userName, String userNickname) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.userNickname = userNickname;
        this.userPassword = generateRandomPassword();
    }

    public UserAccountEntity toEntity(PasswordEncoder passwordEncoder) {
        if (userPassword == null) {
            // userPassword가 null인 경우 랜덤 비밀번호를 생성합니다.
            this.userPassword = generateRandomPassword();
        }
        
        return UserAccountEntity.builder()
                .userEmail(userEmail)
                .userName(userName)
                .userNickname(userNickname)
                .userPassword(passwordEncoder.encode(userPassword))
                .build();
    }
    private String generateRandomPassword() {
        // 랜덤 비밀번호 생성 로직
        int length = 12;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder randomPassword = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(characters.length());
            randomPassword.append(characters.charAt(index));
        }

        return randomPassword.toString();
    }
}
