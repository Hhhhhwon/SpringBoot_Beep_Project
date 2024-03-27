package com.itwill.beep.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itwill.beep.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RequiredArgsConstructor
@RestController
public class UserAccountRestController {
    private final UserService userService;
    
    @GetMapping("/oauth2/authorization/google")
    public String googleAuthorization() {
        return "redirect:/login/google"; // 실제 로그인 페이지로 리다이렉트
    }


    // 이메일을 받아 사용자 아이디(이름)을 반환하는 API 엔드포인트
    @GetMapping("/user/find-username")
    public ResponseEntity<?> findUserNameByEmail(@RequestParam String email) {
        try {
            String userName = userService.findUserNameByEmail(email);
            return new ResponseEntity<>(userName, HttpStatus.OK);
        } catch (UsernameNotFoundException e) {
            // 에러가 발생하면 사용자에게 적절한 HTTP 상태 코드와 메시지 반환
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}