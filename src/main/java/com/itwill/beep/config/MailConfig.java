package com.itwill.beep.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("dlvlfdn567@gmail.com");
        mailSender.setPassword("kvluldvhyqcvzwhm");

        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.transport.protocol", "smtp"); // 프로토콜로 smtp 사용
        javaMailProperties.put("mail.smtp.auth", "true"); // smtp 서버에 인증이 필요
        //gmail의 경우 보안문제 업데이트로 인해 SSLSocketFactory를 추가해야 smtp 사용 가능.
        javaMailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        javaMailProperties.put("mail.smtp.starttls.enable", "true"); // STARTTLS(TLS를 시작하는 명령)를 사용하여 암호화된 통신을 활성
        javaMailProperties.put("mail.debug", "true"); // 디버깅 정보 출력
        javaMailProperties.put("mail.smtp.ssl.trust", "smtp.gmail.com"); // smtp 서버의 ssl 인증서를 신뢰
        javaMailProperties.put("mail.smtp.ssl.protocols", "TLSv1.2"); // 사용할 ssl 프로토콜 버전

        mailSender.setJavaMailProperties(javaMailProperties);

        return mailSender;
    }
}