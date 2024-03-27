package com.itwill.beep.web;

import java.io.IOException;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.domain.UserAccountRepository;
import com.itwill.beep.dto.ChannelImageRequestDto;
import com.itwill.beep.dto.FollowerListRequestDto;
import com.itwill.beep.dto.FollowerSearchRequestDto;
import com.itwill.beep.dto.StreamingOnDto;
import com.itwill.beep.service.ChannelService;
import com.itwill.beep.service.FollowService;
import com.itwill.beep.service.ImageService;
import com.itwill.beep.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/settings")
public class ProfileController {

    private final UserAccountRepository userAccountRepository;
    private final UserService userService;
    private final ImageService imageService;
    private final ChannelService channelService;
    private final FollowService followService;


    @GetMapping("/profile")
    public String profileSettings(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authentication.getPrincipal() instanceof OAuth2User) {
            // OAuth2 사용자의 정보
            String oauth2name = (String) ((OAuth2User) principal).getAttributes().get("name");
            log.info("oauth2name = {}", oauth2name);

            UserAccountEntity loginUser = userAccountRepository.findByUserName(oauth2name);

            model.addAttribute("active", "profile"); // 현재 활성화된 섹션을 모델에 추가

            ChannelEntity channelEntity = channelService.findChannelByUserAccount(loginUser);

            model.addAttribute("channel", channelEntity);
            // fragments 에 필요한 모델
            model.addAttribute("userAccount", loginUser);

            // myModal에 필요한 모델
            ChannelEntity forModal = channelService.findChannelByUserAccount(loginUser);
            model.addAttribute("channel", forModal);
            if (forModal != null) {
                String status = forModal.getStreamingStateSet().toString();
                model.addAttribute("status", status);
            }

        } else {
            String username = authentication.getName();
            UserAccountEntity loginUser = userService.findUserByUserName(username);

            model.addAttribute("active", "profile"); // 현재 활성화된 섹션을 모델에 추가

            ChannelEntity channelEntity = channelService.findChannelByUserAccount(loginUser);

            model.addAttribute("channel", channelEntity);
            // fragments 에 필요한 모델
            model.addAttribute("userAccount", loginUser);

            // myModal에 필요한 모델
            ChannelEntity forModal = channelService.findChannelByUserAccount(loginUser);
            model.addAttribute("channel", forModal);
            if (forModal != null) {
                String status = forModal.getStreamingStateSet().toString();
                model.addAttribute("status", status);
            }

        }

        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam("username") String username,
            @RequestParam("nickname") String nickname,
            @RequestParam("selfIntroduction") String selfIntroduction, // 이메일 인증 로직 필요
            Model model) {
        // 프로필 업데이트 로직
        userService.updateProfile(username, nickname, selfIntroduction);
        return "redirect:/settings/profile?success";
    }

    @PostMapping("/channelupdate")
    public String channelImage(@ModelAttribute ChannelImageRequestDto dto) throws IOException {
        log.info("dto={}", dto);

        imageService.uploadImageToS3(dto);

        return "redirect:/settings/profile";
    }

    @GetMapping("/followers-list")
    public String followerPage(@RequestParam(name = "p", defaultValue = "0") int p, Model model) {
        log.info("followerPage(p={})", p);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Page<FollowerListRequestDto> followersList = null;
        UserAccountEntity loginUser = null;
        Long followersCount = 0L;
        if (authentication.getPrincipal() instanceof OAuth2User) {
            loginUser = userService.findUserByUserName(
                    (String) ((OAuth2User) principal).getAttributes().get("name"));
            followersCount = followService.countFollowers(loginUser);
            followersList = followService.followingList(
                    (String) ((OAuth2User) principal).getAttributes().get("name"), p);
        } else if (authentication.isAuthenticated()) {
            loginUser = userService.findUserByUserName(authentication.getName());
            followersCount = followService.countFollowers(loginUser);
            followersList = followService.followingList(authentication.getName(), p);
        } else {
            log.info("로그인되지 않은 사용자입니다.");
        }

        log.info("followerList={}", followersList);
        model.addAttribute("followersCount", followersCount);
        model.addAttribute("followersList", followersList);

        // fragments 에 필요한 모델
        model.addAttribute("userAccount", loginUser);

        // myModal에 필요한 모델
        ChannelEntity forModal = channelService.findChannelByUserAccount(loginUser);
        model.addAttribute("channel", forModal);
        if (forModal != null) {
            String status = forModal.getStreamingStateSet().toString();
            model.addAttribute("status", status);
        }

        return "follower";
    }

    @GetMapping("/followers-search")
    public String followerSearch(@ModelAttribute FollowerSearchRequestDto dto, Model model) {
        log.info("followerSearch(dto={})", dto);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Page<FollowerListRequestDto> searchResult = null;
        UserAccountEntity loginUser = null;
        Long followersCount = 0L;
        if (authentication.getPrincipal() instanceof OAuth2User) {
            loginUser = userService.findUserByUserName(
                    (String) ((OAuth2User) principal).getAttributes().get("name"));
            followersCount = followService.countFollowers(loginUser);
            dto.setFollowingUserAccountUserName(
                    (String) ((OAuth2User) principal).getAttributes().get("name"));
            searchResult = followService.search(dto);
            log.info("searchResult={}", searchResult);
        } else if (authentication.isAuthenticated()) {
            loginUser = userService.findUserByUserName(authentication.getName());
            followersCount = followService.countFollowers(loginUser);
            dto.setFollowingUserAccountUserName(authentication.getName());
            searchResult = followService.search(dto);
            log.info("searchResult={}", searchResult);
        } else {
            log.info("로그인되지 않은 사용자입니다.");
        }

        model.addAttribute("followersCount", followersCount);
        model.addAttribute("searchResult", searchResult);

        // fragments 에 필요한 모델
        model.addAttribute("userAccount", loginUser);

        // myModal에 필요한 모델
        ChannelEntity forModal = channelService.findChannelByUserAccount(loginUser);
        model.addAttribute("channel", forModal);
        if (forModal != null) {
            String status = forModal.getStreamingStateSet().toString();
            model.addAttribute("status", status);
        }

        return "follower";
    }

    @GetMapping("/channelinfo")
    public String channelInfo(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authentication.getPrincipal() instanceof OAuth2User) {
            // OAuth2 사용자의 정보
            String oauth2name = (String) ((OAuth2User) principal).getAttributes().get("name");
            log.info("oauth2name = {}", oauth2name);

            UserAccountEntity loginUser = userAccountRepository.findByUserName(oauth2name);

            model.addAttribute("active", "profile"); // 현재 활성화된 섹션을 모델에 추가

            ChannelEntity channelEntity = channelService.findChannelByUserAccount(loginUser);

            model.addAttribute("channel", channelEntity);
            // fragments 에 필요한 모델
            model.addAttribute("userAccount", loginUser);

            // myModal에 필요한 모델
            ChannelEntity forModal = channelService.findChannelByUserAccount(loginUser);
            model.addAttribute("channel", forModal);
            if (forModal != null) {
                String status = forModal.getStreamingStateSet().toString();
                model.addAttribute("status", status);
            }

        } else {
            String username = authentication.getName();
            UserAccountEntity loginUser = userService.findUserByUserName(username);

            model.addAttribute("active", "profile"); // 현재 활성화된 섹션을 모델에 추가

            ChannelEntity channelEntity = channelService.findChannelByUserAccount(loginUser);

            model.addAttribute("channel", channelEntity);
            // fragments 에 필요한 모델
            model.addAttribute("userAccount", loginUser);

            // myModal에 필요한 모델
            ChannelEntity forModal = channelService.findChannelByUserAccount(loginUser);
            model.addAttribute("channel", forModal);
            if (forModal != null) {
                String status = forModal.getStreamingStateSet().toString();
                model.addAttribute("status", status);
            }

        }

        return "settingsChannelInfo";
    }

    @PostMapping("/channelinfo")
    public String channelInfoUpdate(@ModelAttribute StreamingOnDto dto) {

        channelService.update(dto);

        return "redirect:/settings/channelinfo";
    }

}
