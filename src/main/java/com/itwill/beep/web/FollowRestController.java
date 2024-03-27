package com.itwill.beep.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.itwill.beep.domain.FollowEntity;
import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.dto.ChannelRequestDto;
import com.itwill.beep.service.ChannelService;
import com.itwill.beep.service.FollowService;
import com.itwill.beep.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/follow")
public class FollowRestController {
    private final FollowService followService;
    private final UserService userService;
    private final ChannelService channelService;

    @PostMapping("/add/{following}")
    public ResponseEntity<Boolean> follow(@PathVariable("following") Long following) {
        log.info("follow(following: {})", following);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (authentication.getPrincipal() instanceof OAuth2User) {
            UserAccountEntity followerEntity = userService.findUserByUserName(
                    (String) ((OAuth2User) principal).getAttributes().get("name"));
            UserAccountEntity followingEntity = userService.findByUserId(following);
            followService.follow(followerEntity, followingEntity);
        } else if (authentication.isAuthenticated()) {
            UserAccountEntity followerEntity =
                    userService.findUserByUserName(authentication.getName());
            UserAccountEntity followingEntity = userService.findByUserId(following);
            followService.follow(followerEntity, followingEntity);
        } else {
            log.info("로그인되지 않은 사용자입니다.");
        }

        return ResponseEntity.ok(true);
    }

    @DeleteMapping("/delete/{following}")
    public ResponseEntity<Boolean> unfollow(@PathVariable("following") Long following) {
        log.info("unfollow(following: {})", following);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (authentication.getPrincipal() instanceof OAuth2User) {
            UserAccountEntity loggedInUser = userService.findUserByUserName(
                    (String) ((OAuth2User) principal).getAttributes().get("name"));
            UserAccountEntity unfollowUser = userService.findByUserId(following);
            followService.unfollow(loggedInUser, unfollowUser);
        } else if (authentication.isAuthenticated()) {
            UserAccountEntity loggedInUser =
                    userService.findUserByUserName(authentication.getName());
            UserAccountEntity unfollowUser = userService.findByUserId(following);

            followService.unfollow(loggedInUser, unfollowUser);
            log.info("{} 님이 언팔로우합니다.", loggedInUser.getUserName());
        } else {
            log.info("로그인되지 않은 사용자입니다.");
        }

        return ResponseEntity.ok(true);
    }

    @GetMapping("/status/{following}")
    public ResponseEntity<Map<String, Boolean>> getFollowStatus(
            @PathVariable("following") Long following) {
        log.info("getFollowStatus(following: {})", following);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        boolean isFollowing = false;
        if (authentication.getPrincipal() instanceof OAuth2User) {
            UserAccountEntity followerEntity = userService.findUserByUserName(
                    (String) ((OAuth2User) principal).getAttributes().get("name"));
            UserAccountEntity followingEntity = userService.findByUserId(following);
            isFollowing = followService.isFollowing(followerEntity, followingEntity);
        } else if (authentication.isAuthenticated()) {
            UserAccountEntity followerEntity =
                    userService.findUserByUserName(authentication.getName());
            UserAccountEntity followingEntity = userService.findByUserId(following);
            isFollowing = followService.isFollowing(followerEntity, followingEntity);
        } else {
            log.info("로그인되지 않은 사용자입니다.");
        }

        Map<String, Boolean> response = new HashMap<>();
        response.put("isFollowing", isFollowing);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/followlist")
    public ResponseEntity<Map<String, Object>> getFollowerList() {
        log.info("getFollowerList()");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<ChannelRequestDto> channelList = new ArrayList<>();
        Long countByfollower = 0L;
        if (authentication.getPrincipal() instanceof OAuth2User) {
            UserAccountEntity followerEntity = userService.findUserByUserName(
                    (String) ((OAuth2User) principal).getAttributes().get("name"));
            countByfollower = followService.countFollowings(followerEntity);
            channelList = channelService.getChannelListForFollowings(followerEntity);
        } else if (authentication.isAuthenticated()) {
            UserAccountEntity followerEntity =
                    userService.findUserByUserName(authentication.getName());
            countByfollower = followService.countFollowings(followerEntity);
            channelList = channelService.getChannelListForFollowings(followerEntity);
        } else {
            log.info("로그인되지 않은 사용자입니다.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("countByFollow", countByfollower);
        response.put("channelList", channelList);
        log.info("response: {}", response);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/followerlist/{following}")
    public ResponseEntity<Map<String, Object>> getFollowingCount(
            @PathVariable("following") Long following) {
        log.info("getFollowingCount(following: {})", following);

        UserAccountEntity followingEntity = userService.findByUserId(following);
        Long countFollowerByfollowing = followService.countFollowers(followingEntity);
        List<FollowEntity> followerList = followService.getFollowers(followingEntity);

        Map<String, Object> response = new HashMap<>();
        response.put("countByFollower", countFollowerByfollowing);
        response.put("followerList", followerList);

        return ResponseEntity.ok(response);
    }

}
