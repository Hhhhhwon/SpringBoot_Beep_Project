package com.itwill.beep.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    // 메시지 타입 : 입장, 채팅, 나감
    public enum MessageType {
        ENTER, TALK, QUIT
    }
    public enum UserType {
        STREAMER, FOLLOW, NON_FOLLOW, ANONYMOUS, BAN, MANAGER
    }
    
    private MessageType type; // 메시지 타입
    private UserType userType; // 유저 타입
    private Long viewers; // 시청자 수
    private Long roomId; // 방번호
    private String sender; // 메시지 보낸사람
    private String message; // 메시지
    
    @Override
    public String toString() {
        return "{\"type\":\"" + type + "\","
            + "\"userType\":\"" + userType + "\","
            + "\"roomId\":" + roomId + ","
            + "\"sender\":\"" + sender + "\","
            + "\"message\":\"" + message + "\","
            + "\"viewers\":\"" + viewers + "\"}";
    }
}
