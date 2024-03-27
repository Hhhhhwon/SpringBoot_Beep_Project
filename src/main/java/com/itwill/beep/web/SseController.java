package com.itwill.beep.web;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.service.SseService;
import com.itwill.beep.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequiredArgsConstructor
@Slf4j
public class SseController {

    // SseService 및 UserService 주입
    private final SseService sseService;
    private final UserService userService;

    // 연결된 사용자들의 SSE를 관리하는 Map
    public static Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    /**
     * SSE 연결을 제공하는 컨트롤러 메소드.
     * @return ResponseEntity<SseEmitter>
     */
    @GetMapping("/connect")
    public ResponseEntity<SseEmitter> notification() {
    	  // 현재 사용자 인증 정보 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // OAuth2 사용자의 이름 정보 초기화
        String oauth2name = null;

        // 사용자 이름 초기화
        String userName = null;

        // OAuth2 사용자의 경우 이름 정보 추출
        if (principal instanceof OAuth2User) {
            oauth2name = (String) ((OAuth2User) principal).getAttributes().get("name");
        }

        // 일반 사용자와 OAuth2 사용자의 경우 사용자 이름 추출
        if (principal instanceof UserDetails) {
            userName = authentication.getName();
        }

        // 사용자가 anonymousUser가 아닌 경우에만 처리
        if (principal instanceof UserDetails) {
            
            // 현재 사용자 정보 조회
            UserAccountEntity user;

            // OAuth2 사용자의 이름이 있는 경우 사용자 이름으로 설정
            if (oauth2name != null) {
                user = userService.findUserByUserName(oauth2name);
            } else {
                user = userService.findUserByUserName(userName);
            }

            Long userId = user.getUserId();

            // SseService를 통해 SSE 연결을 수행하고, 그 결과로 SseEmitter를 반환
            SseEmitter sseEmitter = sseService.connect(userId);

            // ResponseEntity에 SseEmitter를 담아 반환
            return ResponseEntity.ok(sseEmitter);
        } else {            
            // anonymousUser의 경우에는 null 반환 (처리 방법에 대한 추가 고려 필요)
            return null;
        }
    }
}
