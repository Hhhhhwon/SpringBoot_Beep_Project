package com.itwill.beep.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itwill.beep.domain.BannedUserEntity;
import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.ManagerEntity;
import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.dto.UserViewAuthorityDto;
import com.itwill.beep.service.BannedUserService;
import com.itwill.beep.service.ChannelService;
import com.itwill.beep.service.FollowService;
import com.itwill.beep.service.ManagerService;
import com.itwill.beep.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController
public class UserViewAuthorityRestController {
    
    private final UserService userService;
    private final ChannelService channelService;
    private final BannedUserService bannedUserService;
    private final ManagerService managerService;
    private final BannedUserService banService;
    private final FollowService followService;

    @PostMapping("/ban")
    public ResponseEntity<Boolean> userBan(@RequestBody UserViewAuthorityDto dto) {
        
        UserAccountEntity user = userService.findUserByUserNickname(dto.getUserNickname());
        ChannelEntity channel = channelService.findChannelById(dto.getChannelId());
        
        BannedUserEntity bannedUser = BannedUserEntity.builder()
                                                .userId(user)
                                                .channelId(channel)
                                                .build();
        
        boolean banCheck = bannedUserService.isBan(user, channel);
        
        if (banCheck == false) {
            bannedUserService.ban(bannedUser);
        }
        
        
        return ResponseEntity.ok(true);
    }
    
    @PostMapping("/manager")
    public ResponseEntity<Boolean> manager(@RequestBody UserViewAuthorityDto dto) {
        
        UserAccountEntity user = userService.findUserByUserNickname(dto.getUserNickname());
        ChannelEntity channel = channelService.findChannelById(dto.getChannelId());
        
        ManagerEntity manager = ManagerEntity.builder()
                                            .userId(user)
                                            .channelId(channel)
                                            .build();
        
        boolean managerCheck = managerService.isManager(user, channel);
        
        if (managerCheck == false) {
            managerService.manager(manager);
        }
        
        return ResponseEntity.ok(true);
    }
    
    @PostMapping("/managerRevoke")
    public ResponseEntity<Boolean> managerRevoke(@RequestBody UserViewAuthorityDto dto) {
        
        UserAccountEntity user = userService.findUserByUserNickname(dto.getUserNickname());
        ChannelEntity channel = channelService.findChannelById(dto.getChannelId());
        
        ManagerEntity manager = ManagerEntity.builder()
                                            .userId(user)
                                            .channelId(channel)
                                            .build();
        
        boolean managerCheck = managerService.isManager(user, channel);
        
        if (managerCheck == true) {
            managerService.managerRevoke(manager);
        }
        
        return ResponseEntity.ok(true);
    }
    
    @GetMapping("/auth")
    public ResponseEntity<String> auth(@RequestParam(name = "channelId") Long channelId) {
        
        String userType = "";
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        String username;

        if (principal instanceof OAuth2User) {
            // OAuth2 사용자의 경우
            username = (String) ((OAuth2User) principal).getAttributes().get("name");
        } else {
            // 일반 사용자의 경우
            username = authentication.getName();
        }
        
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
        
        return ResponseEntity.ok(userType);
    }
}
