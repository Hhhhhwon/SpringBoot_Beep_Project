package com.itwill.beep.web;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.StreamingState;
import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.dto.ChatRoom;
import com.itwill.beep.service.ChannelService;
import com.itwill.beep.service.ChatService;
import com.itwill.beep.service.FollowService;
import com.itwill.beep.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/dashboard/u/{username}")
public class DashboardController {
    private final UserService userService;
    private final ChannelService channelService;
    private final FollowService followService;
    private final ChatService chatService;

    // 공통 모델 설정 메소드
    private String commonModelSetup(String userName, Authentication authentication, Model model,
            String viewName) {

        UserAccountEntity user = userService.findUserByUserName(userName);

        model.addAttribute("user", user);

        // viewName을 기반으로 경로 접두사를 결정합니다.
        String pathPrefix = determinePathPrefix(viewName);

        log.info("dashboard/user/{}{}", pathPrefix, viewName);
        // 동적으로 결정된 뷰 경로를 반환합니다.
        return String.format("dashboard/user/%s%s", pathPrefix, viewName);
    }

    private String determinePathPrefix(String viewName) {
        // 기본 접두사를 초기화합니다.
        String pathPrefix = "";

        if (viewName.equals("analytics") || viewName.equals("community")
                || viewName.equals("content") || viewName.equals("settings")
                || viewName.equals("viewer-rewards")) {
            // 기본 뷰 이름에 대해서는 접두사 없이 처리합니다.
            pathPrefix = "";
        } else if (viewName.startsWith("analytics")) {
            pathPrefix = "analytics/";
        } else if (viewName.startsWith("community")) {
            pathPrefix = "community/";
        } else if (viewName.startsWith("content")) {
            // 'contentClipsChannel' 같은 특정 패턴을 처리합니다.
            if (viewName.startsWith("contentClips")) {
                // 'content/clips/' 접두사를 추가하여 경로를 구성합니다.
                pathPrefix = "content/clips/";
            } else {
                pathPrefix = "content/";
            }

        } else if (viewName.startsWith("settings")) {
            if (viewName.startsWith("settingsChannel")) {
                // 'content/clips/' 접두사를 추가하여 경로를 구성합니다.
                pathPrefix = "settings/channel/";
            } else {
                pathPrefix = "settings/";
            }

        } else if (viewName.startsWith("viewerRewards")) {
            pathPrefix = "viewerRewards/";
        }

        return pathPrefix;
    }


    // 대시보드 홈
    @GetMapping("/home")
    public String userDashboardHome(@PathVariable("username") String userName,
            Authentication authentication, Model model) {
        log.info("Accessing dashboard home for user: {}", userName);
        return commonModelSetup(userName, authentication, model, "dashboardHome");
    }

    // 방송 관리자
    @GetMapping("/stream-manager")
    public String streamManager(@PathVariable("username") String userName,
            Authentication authentication, Model model) {
        log.info("Accessing stream manager for user: {}", userName);
        return commonModelSetup(userName, authentication, model, "streamManager");
    }

    // 알림
    @GetMapping("/stream-alerts")
    public String streamAlerts(@PathVariable("username") String userName,
            Authentication authentication, Model model) {
        log.info("Accessing stream alerts for user: {}", userName);
        return commonModelSetup(userName, authentication, model, "streamAlerts");
    }

    // 분석 - 한눈에 보기
    @GetMapping("/analytics")
    public String analyticsOverview(@PathVariable("username") String userName,
            Authentication authentication, Model model) {
        log.info("Accessing analytics overview for user: {}", userName);
        return commonModelSetup(userName, authentication, model, "analyticsOverview");
    }

    // 분석 - 연구
    @GetMapping("/analytics/research")
    public String analyticsResearch(@PathVariable("username") String userName,
            Authentication authentication, Model model) {
        log.info("Accessing analytics research for user: {}", userName);
        return commonModelSetup(userName, authentication, model, "analyticsResearch");
    }

    // 분석 - 업적
    @GetMapping("/analytics/achievements")
    public String analyticsAchievements(@PathVariable("username") String userName,
            Authentication authentication, Model model) {
        log.info("Accessing analytics achievements for user: {}", userName);
        return commonModelSetup(userName, authentication, model, "analyticsAchievements");
    }

    // 분석 - 방송 요약
    @GetMapping("/analytics/stream-summary")
    public String analyticsStreamSummary(@PathVariable("username") String userName,
            Authentication authentication, Model model) {
        log.info("Accessing stream summary for user: {}", userName);
        return commonModelSetup(userName, authentication, model, "analyticsStreamSummary");
    }

    // 분석 - 시청자 발견
    @GetMapping("/analytics/viewer-discovery")
    public String analyticsViewerDiscovery(@PathVariable("username") String userName,
            Authentication authentication, Model model) {
        log.info("Accessing viewer discovery for user: {}", userName);
        return commonModelSetup(userName, authentication, model, "analyticsViewerDiscovery");
    }

    // 분석 - 시청자 참여
    @GetMapping("/analytics/viewer-engagement")
    public String analyticsViewerEngagement(@PathVariable("username") String userName,
            Authentication authentication, Model model) {
        log.info("Accessing viewer engagement for user: {}", userName);
        return commonModelSetup(userName, authentication, model, "analyticsViewerEngagement");
    }

    // 커뮤니티 - 역할 관리자
    @GetMapping("/community/roles")
    public String communityRoles(@PathVariable("username") String userName,
            Authentication authentication, Model model) {
        log.info("Accessing community roles for user: {}", userName);
        return commonModelSetup(userName, authentication, model, "communityRoles");
    }

    // 커뮤니티 - 활동
    @GetMapping("/community/activity")
    public String communityActivity(@PathVariable("username") String userName,
            Authentication authentication, Model model) {
        log.info("Accessing community activity for user: {}", userName);
        return commonModelSetup(userName, authentication, model, "communityActivity");
    }

    // 콘텐츠 - 비디오 프로듀서
    @GetMapping("/content/video-producer")
    public String contentVideoProducer(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing video producer for user: {}", username);
        return commonModelSetup(username, authentication, model, "contentVideoProducer");
    }

    // 콘텐츠 - 컬렉션
    @GetMapping("/content/collections")
    public String contentCollections(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing collections for user: {}", username);
        return commonModelSetup(username, authentication, model, "contentCollections");
    }

    // 콘텐츠 - 클립
    @GetMapping("/content/clips/channel")
    public String contentClips(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing clips for user: {}", username);
        return commonModelSetup(username, authentication, model, "contentClipsChannel");
    }

    // 콘텐츠 - 저작권 침해 신고 관리자
    @GetMapping("/content/claims")
    public String contentClaims(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing content claims for user: {}", username);
        return commonModelSetup(username, authentication, model, "contentClaims");
    }

    // 설정 - 방송
    @GetMapping("/settings/stream")
    public String settingsStream(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing stream settings for user: {}", username);
        return commonModelSetup(username, authentication, model, "settingsStream");
    }

    // 설정 - 채널 정보
    @GetMapping("/settings/channel")
    public String settingsChannelInfo(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing channel info settings for user: {}", username);

        // (2) 현재 로그인한 사용자 정보를 가져옵니다.
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("--- home() principal={}", principal);

        // (3) OAuth2User 또는 UserDetails에 따라 처리
        if (principal instanceof OAuth2User) {
            handleOAuth2User((OAuth2User) principal, model);
        } else if (principal instanceof UserDetails) {
            handleUserDetails((UserDetails) principal, model);
        } else {
            log.info("---> ?");
        }

        // (7) 현재 진행 중인 방송의 리스트를 홈으로 보냅니다.
        List<ChatRoom> broadcastList = chatService.findAllRoom();

        // (8) 방송 중인 채널 목록을 가져옵니다.
        List<ChannelEntity> channelList = getActiveChannels(broadcastList);
        model.addAttribute("channelList", channelList);

        return commonModelSetup(username, authentication, model, "settingsChannelInfo");
    }


    // 설정 - 채널 브랜드
    @GetMapping("/settings/channel/brand")
    public String settingsChannelBrand(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing channel brand settings for user: {}", username);
        return commonModelSetup(username, authentication, model, "settingsChannelBrand");
    }

    // 설정 - 채널 추천 콘텐츠
    @GetMapping("/settings/channel/featured-content")
    public String settingsChannelFeaturedContent(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing channel featured content settings for user: {}", username);
        return commonModelSetup(username, authentication, model, "settingsChannelFeaturedContent");
    }

    // 설정 - 채널 일정
    @GetMapping("/settings/channel/schedule")
    public String settingsChannelSchedule(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing channel schedule settings for user: {}", username);
        return commonModelSetup(username, authentication, model, "settingsChannelSchedule");
    }

    // 설정 - 검토
    @GetMapping("/settings/moderation")
    public String settingsModeration(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing moderation settings for user: {}", username);
        return commonModelSetup(username, authentication, model, "settingsModeration");
    }

    // 시청자 보상 - 드롭스
    @GetMapping("/viewer-rewards/drops")
    public String viewerRewardsDrops(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing viewer rewards drops for user: {}", username);
        return commonModelSetup(username, authentication, model, "viewerRewardsDrops");
    }

    // 시청자 보상 - 보상
    @GetMapping("/viewer-rewards/reward-campaigns")
    public String viewerRewardsRewardCampaigns(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing viewer rewards campaigns for user: {}", username);
        return commonModelSetup(username, authentication, model, "viewerRewardsRewardCampaigns");
    }

    // 함께 방송
    @GetMapping("/guest-star")
    public String guestStar(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing guest star for user: {}", username);
        return commonModelSetup(username, authentication, model, "guestStar");
    }

    // 방송 도구
    @GetMapping("/broadcast")
    public String broadcastTools(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing broadcast tools for user: {}", username);
        return commonModelSetup(username, authentication, model, "broadcastTools");
    }

    // 확장 프로그램
    @GetMapping("/extensions")
    public String extensions(@PathVariable("username") String username,
            Authentication authentication, Model model) {
        log.info("Accessing extensions for user: {}", username);
        return commonModelSetup(username, authentication, model, "extensions");
    }

    // (4) OAuth2User 처리
    private void handleOAuth2User(OAuth2User oAuth2User, Model model) {
        String oauth2name = (String) oAuth2User.getAttributes().get("name");
        log.info("---> OAuth2User");
        log.info("---> username={}", oauth2name);

        // (5) 사용자 정보 처리
        handleUser(oauth2name, model);
    }

    // (4) UserDetails 처리
    private void handleUserDetails(UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        log.info("---> UserDetails");
        log.info("---> username={}", username);

        // (5) 사용자 정보 처리
        handleUser(username, model);
    }

    // (5) 사용자 정보 처리
    private void handleUser(String username, Model model) {
        if (!"anonymousUser".equals(username)) {
            UserAccountEntity userAccountEntity = userService.findUserByUserName(username);
            model.addAttribute("userAccount", userAccountEntity);

            // (6) 채널 정보 처리
            ChannelEntity channel = channelService.findChannelByUserAccount(userAccountEntity);
            log.info("channel = {}", channel);
            handleChannel(channel, model);
        }
    }

    // (6) 채널 정보 처리
    private void handleChannel(ChannelEntity channel, Model model) {
        if (channel != null) {
            model.addAttribute("channel", channel);
            String status = channel.getStreamingStateSet().toString();
            log.info("--- home() status={}", status);
            model.addAttribute("status", status);
        } else {
            model.addAttribute("status", "No Channel");
        }
    }

    // (8) 방송 중인 채널 목록을 가져오는 메서드
    private List<ChannelEntity> getActiveChannels(List<ChatRoom> broadcastList) {
        return broadcastList.stream().map(this::convertToChannel)
                .filter(channel -> channel != null
                        && channel.getStreamingStateSet().contains(StreamingState.ON))
                .collect(Collectors.toList());
    }

    // (8) ChatRoom을 ChannelEntity로 변환하는 메서드
    private ChannelEntity convertToChannel(ChatRoom room) {
        Long channelId = room.getRoomId();
        log.info("channelId = {}", channelId);
        ChannelEntity channel = channelService.findChannelById(channelId);
        log.info("channel = {}", channel);
        return channel;
    }
}
