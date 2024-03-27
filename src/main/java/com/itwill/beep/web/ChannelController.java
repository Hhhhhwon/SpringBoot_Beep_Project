package com.itwill.beep.web;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.domain.UserRoleType;
import com.itwill.beep.dto.ChannelRequestDto;
import com.itwill.beep.dto.ChatRoom;
import com.itwill.beep.service.BannedUserService;
import com.itwill.beep.service.ChannelService;
import com.itwill.beep.service.ChatService;
import com.itwill.beep.service.FollowService;
import com.itwill.beep.service.ManagerService;
import com.itwill.beep.service.S3Service;
import com.itwill.beep.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChannelController {

    private final UserService userSvc;
    private final ChatService chatSvc;
    private final ChannelService channelSvc;
    private final FollowService followSvc;
    private final BannedUserService banSvc;
    private final ManagerService managerSvc;
    private final S3Service s3Service;

    @GetMapping("/channel/{id}")
    public String channel(@PathVariable(name = "id") String id, Model model) {
        log.info("{}의 채널", id);
        // SecurityContextHolder는 스프링 시큐리티에서 현재 스레드의 SecurityContext를 관리하는 클래스.
        // SecurityContext는 현재 사용자의 인증 정보와 권한 정보를 포함.

        // Authentication 객체를 얻기 위해 SecurityContextHolder의 getContext() 메소드를 호출.
        // 이 메소드를 통해 현재 스레드의 SecurityContext에 접근할 수 있음.
        // SecurityContext securityContext = SecurityContextHolder.getContext();

        // SecurityContext에서 Authentication 객체를 얻습니다.
        // 현재 사용자의 인증 정보와 권한 정보가 포함된 객체입니다.
        // Authentication authentication = securityContext.getAuthentication();

        // Authentication 인터페이스는 사용자의 인증 정보를 캡슐화한 인터페이스입니다.
        // 주로 사용되는 하위 클래스로는 UsernamePasswordAuthenticationToken, OAuth2AuthenticationToken 등이 있음.
        // Authentication 객체에는 사용자의 주체(Principal), 자격 증명(Credentials), 사용자의 권한들(Granted Authorities)
        // 등이 포함.

        // 예를 들어, 사용자가 로그인하면 Authentication 객체에 해당 사용자의 정보와 권한이 담겨져 있음.
        // 이 정보를 통해 애플리케이션에서 현재 사용자의 권한에 따른 동작을 수행할 수 있음.
        // 로그인한 유저 정보를 가지고 있는 클래스에서 username이 anonymousUser(익명)이 아닐경우에
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authentication.getPrincipal() instanceof OAuth2User) {
            // OAuth2 인증을 통한 사용자 정보 가져오기
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauth2User.getAttributes();

            // 사용자의 name 가져오기
            String oauth2name = (String) ((OAuth2User) principal).getAttributes().get("name");
            log.info("-------- ChannelController username={}", oauth2name);

            // 유저 정보에서 유저의 아이디를 불러온다.
            UserAccountEntity user = userSvc.findUserByUserName(oauth2name);

            // pathvariable로 스트리머 정보를 가져온다
            UserAccountEntity streamer = userSvc.findUserByUserNickname(id);
            log.info("streamer = {}", streamer);

            model.addAttribute("streamer", streamer);

            // streamer 정보로 channel 정보를 불러온다.
            ChannelEntity channel = channelSvc.findChannelByUserAccount(streamer);
            log.info("channel = {}", channel);
            Long channelId = channel.getChannelId();

            // user의 follow 여부를 체크한다.
            boolean followCheck = followSvc.isFollowing(user, streamer);

            // user의 밴 여부를 체크한다.
            boolean banCheck = banSvc.isBan(user, channel);

            // user의 매너저 여부를 체크한다.
            boolean managerCheck = managerSvc.isManager(user, channel);

            // 사용자 상태 설정
            if (user.getUserId() == streamer.getUserId()) {
                model.addAttribute("userState", "STREAMER");
            } else if (managerCheck) {
                model.addAttribute("userState", "MANAGER");
            } else if (banCheck) {
                model.addAttribute("userState", "BAN");
            } else if (followCheck) {
                model.addAttribute("userState", "FOLLOW");
            } else {
                model.addAttribute("userState", "NON_FOLLOW");
            }

            // model에 user를 보낸다.
            log.info("user = {}", user);
            model.addAttribute("userAccount", user);

            // 채팅창 정보를 보낸다.
            String chatState = channel.getChatStateSet().toString();
            model.addAttribute("chatState", chatState);
            log.info("OAuth2 chatState={}", chatState);

            model.addAttribute("channel", channel);

            // channelId로 room 정보를 찾는다.
            ChatRoom room = chatSvc.findRoomById(channelId);
            log.info("room = {}", room);
            model.addAttribute("room", room);


            // channel.status는 Set 타입 객체다. 문자열로 변환하여 전송
            ChannelEntity myChannel = channelSvc.findChannelByUserAccount(user);
            String status = myChannel.getStreamingStateSet().toString();
            model.addAttribute("status", status);



        } else if ("anonymousUser".equals(authentication.getName())) {
            // 비로그인 시청자가 방송을 시청하려고 하는 경우
            UserAccountEntity user =
                    UserAccountEntity.builder().userNickname("anonymousUser").build();
            user.addUserRole(UserRoleType.USER);

            UserAccountEntity streamer = userSvc.findUserByUserNickname(id);
            log.info("streamer = {}", streamer);
            model.addAttribute("streamer", streamer);

            // model에 user를 보낸다.
            model.addAttribute("userAccount", user);
            model.addAttribute("userState", "ANONYMOUS");
            log.info("user = {}", user);

            // streamer정보로 channel정보를 불러온다.
            ChannelEntity channel = channelSvc.findChannelByUserAccount(streamer);
            log.info("channel = {}", channel);
            model.addAttribute("channel", channel);
            Long channelId = channel.getChannelId();

            // 채팅창 정보를 보낸다.
            String chatState = channel.getChatStateSet().toString();
            model.addAttribute("chatState", chatState);

            model.addAttribute("channel", channel);

            // channelId로 room 정보를 찾는다.
            ChatRoom room = chatSvc.findRoomById(channelId);
            log.info("room = {}", room);
            model.addAttribute("room", room);

        } else if (!"anonymousUser".equals(authentication.getName())) {
            // 로그인한 유저 정보를 불러온다.

            // 유저 정보에서 유저의 아이디를 불러온다.
            String username = authentication.getName();
            log.info("username = {}", username);

            // 유저 아이디로 유저의 상세정보를 불러올 쿼리를 실행한다.
            UserAccountEntity user = userSvc.findUserByUserName(username);

            // pathvariable로 스트리머 정보를 가져온다
            UserAccountEntity streamer = userSvc.findUserByUserNickname(id);
            log.info("streamer = {}", streamer);
            model.addAttribute("streamer", streamer);


            // streamer정보로 channel정보를 불러온다.
            ChannelEntity channel = channelSvc.findChannelByUserAccount(streamer);
            log.info("channel = {}", channel);
            Long channelId = channel.getChannelId();

            // user의 follow 여부를 체크한다.
            boolean followCheck = followSvc.isFollowing(user, streamer);

            // user의 밴 여부를 체크한다.
            boolean banCheck = banSvc.isBan(user, channel);

            // user의 매너저 여부를 체크한다.
            boolean managerCheck = managerSvc.isManager(user, channel);

            if (user.getUserId() == streamer.getUserId()) {
                model.addAttribute("userState", "STREAMER");
            } else if (managerCheck == true) {
                model.addAttribute("userState", "MANAGER");
            } else if (banCheck == true) {
                model.addAttribute("userState", "BAN");
            } else if (followCheck == true) {
                model.addAttribute("userState", "FOLLOW");
            } else if (followCheck == false) {
                model.addAttribute("userState", "NON_FOLLOW");
            }
            // model에 user를 보낸다.
            log.info("user = {}", user);
            model.addAttribute("userAccount", user);

            // 채팅창 정보를 보낸다.
            String chatState = channel.getChatStateSet().toString();
            model.addAttribute("chatState", chatState);

            model.addAttribute("channel", channel);

            // channelId로 room 정보를 찾는다.
            ChatRoom room = chatSvc.findRoomById(channelId);
            log.info("room = {}", room);
            model.addAttribute("room", room);

        
                                                                                                // URL
                                                                                                // 동적
        



            // channel.status는 Set타입 객체다 그래서인지 th:if 조건문에서 계속 실패했다.
            // 타임리프로 해결하는 방안이 있을 것이라고 생각은 하지만 공식문서를 뒤져봐도 해결법은 찾지 못했다.
            // 그래서 그냥 컨트롤러 부분에서 문자열로 변환하여 보내기로 했다. (해결법은 찾았지만 결국엔 이게 정답이었다.)
            ChannelEntity myChannel = channelSvc.findChannelByUserAccount(user);
            String status = myChannel.getStreamingStateSet().toString();
            model.addAttribute("status", status);
        }
        return "channel";


    }


    @ResponseBody
    @GetMapping("/api/channel/popular")
    public ResponseEntity<List<ChannelRequestDto>> popularChannel() {
        log.info("popularChannel()");

        List<ChannelRequestDto> data = channelSvc.getPopularChannelList();

        return ResponseEntity.ok(data);
    }

}
