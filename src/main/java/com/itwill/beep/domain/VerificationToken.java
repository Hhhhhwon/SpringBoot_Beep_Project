package com.itwill.beep.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@Entity
@Table(name = "verification_tokens")
public class VerificationToken {

    private static final int EXPIRATION = 60 * 24; // 토큰 유효 시간 24시간

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="verifi_token_id")
    private Long verifiTokenId;

    @Column(name="verifi_token_value")
    private String verifiTokenValue;

    @OneToOne(targetEntity = UserAccountEntity.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "verifi_token_user_Account")
    private UserAccountEntity verifiTokenUserAccount;

    @Column(name="verifi_token_expiry_date")
    private LocalDateTime verifiTokenExpiryDate;

    public VerificationToken() {
        super();
    }

    public VerificationToken(String token, UserAccountEntity user) {
        super();
        this.verifiTokenValue = token;
        this.verifiTokenUserAccount = user;
        this.verifiTokenExpiryDate = calculateExpiryDate(EXPIRATION);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(verifiTokenExpiryDate);
    }

    private LocalDateTime calculateExpiryDate(int expiryTimeInMinutes) {
        return LocalDateTime.now().plus(expiryTimeInMinutes, ChronoUnit.MINUTES);
    }

    // 토큰 만료 시간 설정 (예: 24시간 후 만료)
    public void updateExpiryDate(int hours) {
        LocalDateTime now = LocalDateTime.now();
        this.verifiTokenExpiryDate = now.plusHours(hours);
    }

    // 표준 게터/세터/생성자 ...
}