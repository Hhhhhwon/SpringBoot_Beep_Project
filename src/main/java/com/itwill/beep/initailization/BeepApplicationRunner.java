package com.itwill.beep.initailization;


import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.service.ChannelService;
import com.itwill.beep.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class BeepApplicationRunner implements ApplicationRunner{

    private final ChannelService channelSvc;
    private final ChatService chatSvc;
    
    // 서버가 실행 될 때 initialization할 메서드
    
    // 서버가 실행 될 때 웹소켓 세션이 만들어져있지 않아서 채녈 페이지에 오류가 생기는 것을 발견
    // 방송을 시작할 때 웹소켓 세션을 만드는 것이 원인이었음.
    // 웹소켓 세션이 없을 경우에 채팅창을 숨기는 것도 방법이겠지만
    // 방송이 시작되지 않았을 때에도 채팅창이 유지되길 원하고
    // 채널의 기능 제대로 실행되길 원하기 때문에 서버가 실행 될 때
    // 각 채널의 웹소켓 세션도 실행되게끔 만듬.
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        
        // 모든 채널 정보를 불러옴
        List<ChannelEntity> channelList = channelSvc.findAllChannel();
        
        // 채널 정보로 웹소켓 세션 생성
        channelList.forEach(channel -> createRoom(channel));
        
        
        log.info("애플리케이션 초기화 작업을 수행합니다.");
    }
    
    private void createRoom(ChannelEntity channel) {
        String userNickname = channel.getChannelUserAccountEntity().getUserNickname();
        Long id = channel.getChannelId();
        chatSvc.createRoom(userNickname, id);
        
    }

}
