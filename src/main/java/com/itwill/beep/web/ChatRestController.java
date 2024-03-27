package com.itwill.beep.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.ChatState;
import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.dto.ChatStateAndUserTypeDto;
import com.itwill.beep.service.BannedUserService;
import com.itwill.beep.service.ChannelService;
import com.itwill.beep.service.FollowService;
import com.itwill.beep.service.ManagerService;
import com.itwill.beep.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChannelService channelService;
    private final UserService userService;
    private final FollowService followService;
    private final BannedUserService banService;
    private final ManagerService managerService;
    
    @PostMapping("/follow/{channelId}")
    public ResponseEntity<Boolean> followChat(@PathVariable("channelId") Long channelId) {
        
        ChannelEntity channel = channelService.findChannelById(channelId);
        
        channel.setChatState(ChatState.FOLLOW);
        channelService.save(channel);
        
        log.info("channel = {}" , channel);
        
        
        return ResponseEntity.ok(true);
    }
    
    @PostMapping("/anonymous/{channelId}")
    public ResponseEntity<Boolean> anonChat(@PathVariable("channelId") Long channelId) {
        
        ChannelEntity channel = channelService.findChannelById(channelId);
        
        channel.setChatState(ChatState.ANONYMOUS);
        channelService.save(channel);
        
        return ResponseEntity.ok(true);
    }
    
    @PostMapping("/ice/{channelId}")
    public ResponseEntity<Boolean> iceChat(@PathVariable("channelId") Long channelId) {
        
        ChannelEntity channel = channelService.findChannelById(channelId);
        
        channel.setChatState(ChatState.STREAMER_ONLY);
        channelService.save(channel);
        
        return ResponseEntity.ok(true);
    }
    
    @PostMapping("/default/{channelId}")
    public ResponseEntity<Boolean> defaultChat(@PathVariable("channelId") Long channelId) {
        
        ChannelEntity channel = channelService.findChannelById(channelId);
        
        channel.setChatState(ChatState.DEFAULT);
        channelService.save(channel);
        
        return ResponseEntity.ok(true);
    }
    
 // SecurityContextHolder를 통해 현재 사용자의 인증 정보를 가져오는 메서드.
 // SecurityContextHolder는 Spring Security에서 현재 인증된 사용자의 보안 컨텍스트를 제공하는 클래스.
 // getAuthentication() 메서드를 사용하여 현재의 인증 객체를 가져올 수 있음.
 // 인증 객체에서는 현재 사용자에 대한 정보와 권한 등의 정보가 포함되어 있음.
 // getPrincipal() 메서드를 호출하여 현재 사용자의 주체(principal)를 가져올 수 있음.
 // 주체는 일반적으로 사용자의 식별 정보를 포함하고 있음.
 // 이 메서드를 통해 얻은 principal 객체는 Object 타입으로 반환되므로 해당 객체의 실제 타입을 확인하여 적절히 캐스팅해야함.
    @GetMapping("/chatState/{channelId}")
    public ResponseEntity<ChatStateAndUserTypeDto> chatState(@PathVariable("channelId") Long channelId) {
    	 Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	 
    	  // 타입으로 조건식 깔끔하고 보기좋은듯 나중에다시 리팩토링 중복코드너무많음
        if (principal instanceof UserDetails) {
            
            String userType = "";
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            String username = authentication.getName();
            
            UserAccountEntity user = userService.findUserByUserName(username);

            ChannelEntity channel = channelService.findChannelById(channelId);
            
            UserAccountEntity streamer = channelService.findUserByChannelId(channelId);
            
            boolean followCheck = followService.isFollowing(user, streamer);
            
            boolean banCheck = banService.isBan(user, channel);
            
            boolean managerCheck = managerService.isManager(user, channel);
            
            if (user.getUserId() == channel.getChannelUserAccountEntity().getUserId()){
                userType = "STREAMER";
            } else if(managerCheck == true) {
                userType = "MANAGER";
            } else if(banCheck == true) {
                userType = "BAN";
            } else if(followCheck == true) {
                userType = "FOLLOW";
            } else if(followCheck == false){
                userType = "NON_FOLLOW";
            }
            
            String chatState = channel.getChatStateSet().toString();
            
            ChatStateAndUserTypeDto dto = new ChatStateAndUserTypeDto();
            dto.setChatState(chatState);
            dto.setUserType(userType);
            
            return ResponseEntity.ok(dto);
        } else if(principal instanceof OAuth2User) {
        	 String userType = "";
             
        	String  oauth2name = (String) ((OAuth2User) principal).getAttributes().get("name");
             
             
             
             UserAccountEntity user = userService.findUserByUserName(oauth2name);

             ChannelEntity channel = channelService.findChannelById(channelId);
             
             UserAccountEntity streamer = channelService.findUserByChannelId(channelId);
             
             boolean followCheck = followService.isFollowing(user, streamer);
             
             boolean banCheck = banService.isBan(user, channel);
             
             boolean managerCheck = managerService.isManager(user, channel);
             
             if (user.getUserId() == channel.getChannelUserAccountEntity().getUserId()){
                 userType = "STREAMER";
             } else if(managerCheck == true) {
                 userType = "MANAGER";
             } else if(banCheck == true) {
                 userType = "BAN";
             } else if(followCheck == true) {
                 userType = "FOLLOW";
             } else if(followCheck == false){
                 userType = "NON_FOLLOW";
             }
             
             String chatState = channel.getChatStateSet().toString();
             
             ChatStateAndUserTypeDto dto = new ChatStateAndUserTypeDto();
             dto.setChatState(chatState);
             dto.setUserType(userType);
             
             return ResponseEntity.ok(dto);
        } else {
        	ChannelEntity channel = channelService.findChannelById(channelId);
        	String chatState = channel.getChatStateSet().toString();
        	
        	ChatStateAndUserTypeDto dto = new ChatStateAndUserTypeDto();
        	dto.setChatState(chatState);
        	dto.setUserType("anonymousUser");
        	
        	return ResponseEntity.ok(dto);
        	
        }
    }
}
