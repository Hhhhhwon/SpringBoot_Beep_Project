package com.itwill.beep.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByVerifiTokenValue(String token);
    VerificationToken findByVerifiTokenUserAccount(UserAccountEntity user);
}