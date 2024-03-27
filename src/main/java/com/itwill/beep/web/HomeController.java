package com.itwill.beep.web;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.amazonaws.services.s3.AmazonS3;
import com.itwill.beep.domain.CategoryEntity;
import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.StreamingState;
import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.dto.BeepSearchRequestDto;
import com.itwill.beep.dto.ChatRoom;
import com.itwill.beep.service.CategoryService;
import com.itwill.beep.service.ChannelService;
import com.itwill.beep.service.ChatService;
import com.itwill.beep.service.FollowService;
import com.itwill.beep.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    // (1) 필요한 서비스 주입
    private final UserService userService;
    private final ChatService chatService;
    private final ChannelService channelService;
    private final AmazonS3 amazonS3;
    private final CategoryService categoryService;
    private final FollowService followService;


    @GetMapping("/")
    @PreAuthorize("permitAll")
    public String home(Model model) {
        // (2) 현재 로그인한 사용자 정보를 가져옴.
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("--- home() principal={}", principal);

        String url = amazonS3.getUrl("beepitwill", "default_profile_image.jpg").toString();
        log.info("url = {}", url);
        model.addAttribute("testUrl", url);

        // (3) OAuth2User 또는 UserDetails에 따라 처리
        if (principal instanceof OAuth2User) {
            handleOAuth2User((OAuth2User) principal, model);
        } else if (principal instanceof UserDetails) {
            handleUserDetails((UserDetails) principal, model);
        } else {
            log.info("---> 누구세요..?");
        }

        // (7) 현재 진행 중인 방송의 리스트를 홈으로 보냅니다.
        List<ChatRoom> broadcastList = chatService.findAllRoom();

        // (8) 방송 중인 채널 목록을 가져옵니다.
        List<ChannelEntity> channelList = getActiveChannels(broadcastList);
        model.addAttribute("channelList", channelList);

        return "home";
    }

    @GetMapping("/lives")
    public String lives(Model model) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof OAuth2User) {
            handleOAuth2User((OAuth2User) principal, model);
        } else if (principal instanceof UserDetails) {
            handleUserDetails((UserDetails) principal, model);
        } else {
            log.info("---> 누구세요..?");
        }

        // (7) 현재 진행 중인 방송의 리스트를 홈으로 보냅니다.
        List<ChatRoom> broadcastList = chatService.findAllRoom();

        // (8) 방송 중인 채널 목록을 가져옵니다.
        List<ChannelEntity> channelList = getActiveChannels(broadcastList);
        Collections.sort(channelList,
                Comparator.comparing(ChannelEntity::getChannelViewerCount).reversed());

        List<ChannelEntity> recommendedChannelList = new ArrayList<>(channelList);
        recommendedChannelList.sort(Comparator.comparing(ChannelEntity::getChannelId).reversed());

        model.addAttribute("channelList", channelList);
        model.addAttribute("recommendedChannelList", recommendedChannelList);

        return "lives";
    }

    @GetMapping("/search")
    public String search(Model model) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof OAuth2User) {
            handleOAuth2User((OAuth2User) principal, model);
        } else if (principal instanceof UserDetails) {
            handleUserDetails((UserDetails) principal, model);
        } else {
            log.info("---> ?");
        }

        return "homesearch";
    }

    @PostMapping("/search")
    public String searchBeep(BeepSearchRequestDto dto, Model model) {
        log.info("searchBeep(dto={})", dto);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof OAuth2User) {
            handleOAuth2User((OAuth2User) principal, model);
        } else if (principal instanceof UserDetails) {
            handleUserDetails((UserDetails) principal, model);
        } else {
            log.info("---> ?");
        }

        List<ChannelEntity> channelUserAccountEntitySearchResult =
                channelService.findChannelBychannelUserAccountEntity(dto.getKeyword());
        List<ChannelEntity> channelSearchResult =
                channelService.findChannelByChannelTitle(dto.getKeyword());
        List<CategoryEntity> categorySearchResult = categoryService.searchGames(dto.getKeyword());
        if (dto.getKeyword().toLowerCase().contains("just")
                || dto.getKeyword().toLowerCase().contains("chat")) {
            categorySearchResult.add(categoryService.findByCategoryIdIs(100_000_000L));
        }
        List<Long> channelFollowerCount = new ArrayList<>();

        for (ChannelEntity entity : channelUserAccountEntitySearchResult) {
            channelFollowerCount
                    .add(followService.countFollowers(entity.getChannelUserAccountEntity()));
        }
        List<ChannelEntity> StreamingStateOnchannelSearchResult = channelSearchResult.stream()
                .filter(t -> t.getStreamingStateSet().toString().contains("ON"))
                .collect(Collectors.toList());

        model.addAttribute("channelUserAccountEntitySearchResult",
                channelUserAccountEntitySearchResult);
        model.addAttribute("channelSearchResult", StreamingStateOnchannelSearchResult);
        model.addAttribute("categorySearchResult", categorySearchResult);
        model.addAttribute("channelFollowerCount", channelFollowerCount);

        return "homesearch";
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
