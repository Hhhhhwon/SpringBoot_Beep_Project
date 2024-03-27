package com.itwill.beep.web;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.FollowEntity;
import com.itwill.beep.domain.StreamingState;
import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.dto.ChatRoom;
import com.itwill.beep.dto.StreamingOnDto;
import com.itwill.beep.service.ChannelService;
import com.itwill.beep.service.ChatService;
import com.itwill.beep.service.FollowService;
import com.itwill.beep.service.S3Service;
import com.itwill.beep.service.SseService;
import com.itwill.beep.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/streaming")
public class StreamingController {

    private final UserService userService;
    private final ChatService chatService;
    private final ChannelService channelService;
    private final S3Service s3Service;
    private final SseService sseService;
    private final FollowService followService;

    @PostMapping("/on")
    public String StreamingOn(Model model, StreamingOnDto streamingOnDto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (authentication.getPrincipal() instanceof OAuth2User) {
            // OAuth2 사용자의 정보
            String oauth2name = (String) ((OAuth2User) principal).getAttributes().get("name");
            log.info("oauth2name = {}", oauth2name);

            // OAuth2 사용자의 정보로 사용자를 찾음
            UserAccountEntity user = userService.findUserByUserName(oauth2name);
            model.addAttribute("userAccount", user);
            model.addAttribute("streamer", user);
            model.addAttribute("userState", "STREAMER");

            // OAuth2 사용자의 채널 정보
            ChannelEntity channel = channelService.findChannelByUserAccount(user);
            channel.setStreamingState(StreamingState.ON);
            channelService.update(streamingOnDto);

            // 업데이트 후 다시 불러오기
            channel = channelService.findChannelByUserAccount(user);

            log.info("channel = {}", channel);
            model.addAttribute("channel", channel);
            Long channelId = channel.getChannelId();

            ChatRoom room = chatService.findRoomById(channelId);
            log.info("first check = {}", room);
            if (room == null) {
                chatService.createRoom(oauth2name, channelId);
                room = chatService.findRoomById(channelId);
                log.info("2 room = {}", room);
                model.addAttribute("room", room);
            } else {
                log.info("3 room = {}", room);
                model.addAttribute("room", room);
            }

            // TODO: 브로드캐스트 상태를 온으로 만들고 팔로워에게 알림을 보내도록
            List<FollowEntity> followers = followService.getFollowers(user);

            List<UserAccountEntity> followUser = followers.stream()
                    .map(FollowEntity::getFollowerUserAccount).collect(Collectors.toList());

            List<Long> followersId = followUser.stream().map(UserAccountEntity::getUserId)
                    .collect(Collectors.toList());

            sseService.notification(user, followersId);

            String status = channel.getStreamingStateSet().toString();
            model.addAttribute("status", status);
            log.info("----  Steaming() status={}", status);

            return "/channel";
        } else if (!"anonymousUser"
                .equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            // 로그인한 사용자
            String username = authentication.getName();
            log.info("username = {}", username);

            // 로그인한 사용자의 정보
            UserAccountEntity user = userService.findUserByUserName(username);
            model.addAttribute("userAccount", user);
            model.addAttribute("streamer", user);
            model.addAttribute("userState", "STREAMER");

            // 로그인한 사용자의 채널 정보
            ChannelEntity channel = channelService.findChannelByUserAccount(user);
            channel.setStreamingState(StreamingState.ON);
            channelService.update(streamingOnDto);

            // 업데이트 후 다시 불러오기
            channel = channelService.findChannelByUserAccount(user);

            log.info("channel = {}", channel);
            model.addAttribute("channel", channel);
            Long channelId = channel.getChannelId();

            ChatRoom room = chatService.findRoomById(channelId);
            log.info("first check = {}", room);
            if (room == null) {
                chatService.createRoom(username, channelId);
                room = chatService.findRoomById(channelId);
                log.info("2 room = {}", room);
                model.addAttribute("room", room);
            } else {
                log.info("room = {}", room);
                model.addAttribute("room", room);
            }

            // TODO: 브로드캐스트 상태를 온으로 만들고 팔로워에게 알림을 보내도록
            List<FollowEntity> followers = followService.getFollowers(user);

            List<UserAccountEntity> followUser = followers.stream()
                    .map(FollowEntity::getFollowerUserAccount).collect(Collectors.toList());

            List<Long> followersId = followUser.stream().map(UserAccountEntity::getUserId)
                    .collect(Collectors.toList());
            new Thread(() -> {
                sseService.notification(user, followersId);
            }).start();


            String status = channel.getStreamingStateSet().toString();
            model.addAttribute("status", status);
            log.info("----  Steaming() status={}", status);

            return "/channel";
        }
        return "/";
    }

    @PostMapping("/off")
    public String broadcastOff() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof OAuth2User) {
            // OAuth2 사용자의 정보
            String oauth2name = (String) ((OAuth2User) principal).getAttributes().get("name");
            UserAccountEntity user = userService.findUserByUserName(oauth2name);

            ChannelEntity channel = channelService.findChannelByUserAccount(user);
            channel.setStreamingState(StreamingState.OFF);
            /* 방송 OFF시 시청자 수 조정하는 메서드 추가했습니다. 확인 필요 */
            channel.resetTotalViewerCount(0L);
            channelService.save(channel);

            return "redirect:/";
        } else {
            // 일반 사용자의 정보
            String username = authentication.getName();
            UserAccountEntity user = userService.findUserByUserName(username);

            ChannelEntity channel = channelService.findChannelByUserAccount(user);
            channel.setStreamingState(StreamingState.OFF);
            /* 방송 OFF시 시청자 수 조정하는 메서드 추가했습니다. 확인 필요 */
            channel.resetTotalViewerCount(0L);
            channelService.save(channel);

            return "redirect:/";
        }
    }

    @GetMapping("/generate-streaming-key")
    public ResponseEntity<?> generateStreamingKey(Authentication authentication) {
        String userName = authentication.getName();
        String streamingKey = userService.generateStreamingKey();

        // 로그 추가: 스트리밍 키 발급 로그
        log.info("스트리밍 키를 발급받았습니다. userName: {}, streamingKey: {}", userName, streamingKey);

        return ResponseEntity.ok().body(Map.of("streamingKey", streamingKey));
    }

    @GetMapping("/re-generate-streaming-key")
    public ResponseEntity<?> reGenerateStreamingKey(Authentication authentication) {
        String userName = authentication.getName();
        String streamingKey = userService.reGenerateStreamingKey(userName);

        // 로그 추가: 스트리밍 키 발급 로그
        log.info("스트리밍 키를 재발급받았습니다. userName: {}, streamingKey: {}", userName, streamingKey);

        return ResponseEntity.ok().body(Map.of("streamingKey", streamingKey));
    }

    @PostMapping("/validate-streaming-key")
    public ResponseEntity<?> validateStreamingKey(
            @RequestBody Map<String, String> streamingKeyRequest) {
        log.info("Received streaming key validation request: {}", streamingKeyRequest);
        String streamingKey = streamingKeyRequest.get("streamingKey");
        boolean streamingKeyIsValid = userService.validateStreamingKey(streamingKey);
        if (streamingKeyIsValid) {
            // 로그 추가: 스트리밍 키 유효성 검사 성공 로그
            log.info("스트리밍 키 유효성 검사가 성공했습니다. streamingKey: {}", streamingKey);

            return ResponseEntity.ok().build();
        } else {
            // 로그 추가: 스트리밍 키 유효성 검사 실패 로그
            log.warn("스트리밍 키 유효성 검사가 실패했습니다. streamingKey: {}", streamingKey);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
