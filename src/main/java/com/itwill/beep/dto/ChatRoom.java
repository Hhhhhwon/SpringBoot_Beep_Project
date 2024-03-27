package com.itwill.beep.dto;

import java.util.HashSet;
import java.util.Set;

import org.springframework.web.socket.WebSocketSession;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatRoom {
    
    private Long roomId;
    
    private String name;
    
    private Set<WebSocketSession> sessions = new HashSet<>();

    @Builder
    public ChatRoom(Long roomId, String name, Long viewers) {
        this.roomId = roomId;
        this.name = name;
    }
}
