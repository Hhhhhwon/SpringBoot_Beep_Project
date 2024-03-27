package com.itwill.beep.handler;

import java.io.IOException;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itwill.beep.dto.ChatMessage;
import com.itwill.beep.dto.ChatRoom;
import com.itwill.beep.service.ChannelService;
import com.itwill.beep.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSockChatHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final ChannelService channelService;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
        ChatRoom room = chatService.findRoomById(chatMessage.getRoomId());
        Set<WebSocketSession> sessions=room.getSessions();   //방에 있는 현재 사용자 한명이 WebsocketSession
        if (chatMessage.getType().equals(ChatMessage.MessageType.ENTER)) {
            //사용자가 방에 입장하면  Enter메세지를 보내도록 해놓음.  이건 새로운사용자가 socket 연결한 것이랑은 다름.
            //socket연결은 이 메세지 보내기전에 이미 되어있는 상태
            sessions.add(session);
            Long viewers = (long) sessions.size();
            chatMessage.setViewers(viewers);
            
            log.info("{}", chatMessage);
            
            TextMessage newMessage = new TextMessage(chatMessage.toString());
            
            sendToEachSocket(sessions, newMessage);
            
            Long channelId = room.getRoomId();
            channelService.setViewers(channelId, viewers);
            
        }else if (chatMessage.getType().equals(ChatMessage.MessageType.QUIT)) {
            sessions.remove(session);
            Long viewers = (long) sessions.size();
            
            chatMessage.setViewers(viewers);
            
            TextMessage newMessage = new TextMessage(chatMessage.toString());
            
            sendToEachSocket(sessions, newMessage);
            
            Long channelId = room.getRoomId();
            channelService.setViewers(channelId, viewers);
            
        }else {
            Long viewers = (long) sessions.size();
            
            chatMessage.setViewers(viewers);
            
            TextMessage newMessage = new TextMessage(chatMessage.toString());
            
            sendToEachSocket(sessions, newMessage); //입장,퇴장 아닐 때는 클라이언트로부터 온 메세지 그대로 전달.
        }
    }
    private  void sendToEachSocket(Set<WebSocketSession> sessions, TextMessage message){
        sessions.parallelStream().forEach( roomSession -> {
            try {
                roomSession.sendMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

    }
}
