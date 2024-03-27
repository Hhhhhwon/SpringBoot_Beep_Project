package com.itwill.beep.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.web.SseController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

/**
 * Server-Sent Events (SSE)를 통한 실시간 통신을 제공하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {
    
    // ChannelService 주입
    private final ChannelService channelService;
    
    /**
     * 사용자와 서버 간에 SSE를 통한 연결을 제공하는 메소드.
     * @param userId 연결하려는 사용자의 고유 식별자
     * @return SseEmitter 객체
     */
    public SseEmitter connect(Long userId) {
        
        // SSE를 생성하고, 타임아웃을 최대치(Long.MAX_VALUE)로 설정
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE); 
        
        try {
            // 클라이언트에게 'connect' 이벤트를 전송
            sseEmitter.send(SseEmitter.event()
                        .name("connect")
                        .data("connect!"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 연결된 SSE를 Map에 저장
        SseController.sseEmitters.put(userId, sseEmitter);
        int size = SseController.sseEmitters.size();
        log.info("Number of connected users = {}", size);
        
        // SSE 연결이 완료되거나 타임아웃 또는 에러가 발생할 경우 Map에서 제거
        sseEmitter.onCompletion(() -> SseController.sseEmitters.remove(userId));
        sseEmitter.onTimeout(() -> SseController.sseEmitters.remove(userId));
        sseEmitter.onError((e) -> SseController.sseEmitters.remove(userId));
        
        return sseEmitter;
    }
    
    /**
     * 사용자와 그의 팔로워들에게 알림을 전송하는 메소드.
     * @param user 알림을 보낼 사용자 정보
     * @param followersId 팔로워들의 고유 식별자 목록
     */
    public void notification(UserAccountEntity user, List<Long> followersId) {
        
        // 사용자의 채널 정보 조회
        ChannelEntity channel = channelService.findChannelByUserAccount(user);
        
        // 팔로워들에게 알림 메시지 전송
        for(Long followerId : followersId) {
            if (SseController.sseEmitters.containsKey(followerId)) {       
                log.info("FollowerId = {}", followerId);
                SseEmitter sseEmitterReceiver = SseController.sseEmitters.get(followerId);
                
                // 알림 메시지를 JSON 형식으로 포장하여 전송
                try {
                    JSONObject data = new JSONObject();
                    data.put("profileImageUrl", channel.getChannelProfileImg());
                    data.put("title", channel.getChannelTitle());
                    data.put("streamer", user.getUserNickname());
                    
                    sseEmitterReceiver.send(SseEmitter.event()
                                                .name("broadcast_on")
                                                .data(data));
                    log.info("SseEmitter = {}", sseEmitterReceiver);
                } catch (Exception e) {
                    // 에러 발생 시 해당 팔로워의 SSE 연결을 제거
                    SseController.sseEmitters.remove(followerId);
                }
            }            
        }
    }
}