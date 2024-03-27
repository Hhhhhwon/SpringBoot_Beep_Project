package com.itwill.beep.domain;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final ChannelRepository channelRepository;

    /**
     * OAuth2 사용자 정보를 처리하는 메소드.
     *
     * @param userRequest OAuth2 사용자 정보 요청 객체
     * @return 처리된 OAuth2 사용자 객체
     * @throws OAuth2AuthenticationException OAuth2 인증 예외
     */
    // OAuth2UserRequest 클래스는 스프링 시큐리티에서 OAuth 2.0 로그인 프로세스에서 사용되는 중요한 클래스.
    // 이 클래스는 OAuth 2.0 공급자(예: Google, Facebook)로부터 사용자 정보를 요청하고 가져오기 위한 필수 정보들을 담고 있음.
    // 간단히 말해서,  소셜 미디어 계정으로 로그인하려고 할 때, 스프링 시큐리티는 이 클래스를 사용하여 해당 계정에서
    // 사용자 정보를 가져오는데 필요한 정보들을 설정하고 전달. 이를 통해 사용자는 간편하게 소셜 미디어 계정으로 로그인할 수 있고,
    // 애플리케이션은 필요한 사용자 정보를 쉽게 획득할 수 있음.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 부모 클래스의 메소드를 호출하여 OAuth2 사용자 정보를 가져옴
        OAuth2User oauth2User = super.loadUser(userRequest);

        // OAuth2 사용자 정보에서 속성들을 추출
        Map<String, Object> attributes = oauth2User.getAttributes();
        String userName = (String) attributes.get("name");
        String userEmail = (String) attributes.get("email");
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
      
        
        
        
        UserAccountEntity userEntity;

        // 소셜 정보 유효성 검사
        validateSocialInfo(userEmail, userName);

        if ("kakao".equals(registrationId)) {
            // 카카오 로그인 처리
            userEntity = userAccountRepository.findByUserEmail(userEmail)
                    .orElseGet(() -> createUserAndChannel(userEmail, userName, registrationId));
        } else if ("google".equals(registrationId)) {
            // 구글 로그인 처리
            userEntity = userAccountRepository.findByUserEmail(userEmail)
                    .orElseGet(() -> createUserAndChannel(userEmail, userName, registrationId));
        } else {
            throw new OAuth2AuthenticationException("Unsupported social media login");
        }
     // 5. OAuth2 사용자의 권한을 설정
        Set<GrantedAuthority> authorities = userEntity.getUserRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());

        // 6. OAuth2User 객체를 새로 생성하여 반환
        // 'sub'은 OAuth2User의 Principal을 나타내는데, 일반적으로 사용자의 고유 식별자를 의미.
        // 여기서는 사용자의 고유 식별자를 'sub'로 설정하여 DefaultOAuth2User를 생성.
        // authorities: 사용자의 권한 정보
        // attributes: OAuth2 사용자의 속성 정보

        // 주의: 'sub'은 사용자의 고유 식별자로 사용되며, 일반적으로 OAuth2에서 제공하는 토큰에서 찾을 수 있음.
        // 사용자를 식별하는데 사용되는 값이므로 변경하지 않도록 주의.

        return new DefaultOAuth2User(authorities, attributes, "sub");
    }
    


    /**
     * SocialDto의 정보가 유효한지 검증.
     * 
     * @param userEmail 사용자 이메일
     * @param userName  사용자 이름
     * @throws OAuth2AuthenticationException SocialDto 정보가 유효하지 않을 때 발생하는 예외
     */
    private void validateSocialInfo(String userEmail, String userName) {
        if (userEmail == null || userName == null) {
            throw new OAuth2AuthenticationException("유효하지 않은 사용자 정보");
        }
    }

    /**
     * 새로운 사용자 및 채널을 생성하고 저장.
     * 
     * @param userEmail 사용자 이메일
     * @param userName  사용자 이름
     * @return 저장된 사용자 객체
     */
    private UserAccountEntity createUserAndChannel(String userEmail, String userName, String registrationId) {
        // 유저 닉네임 생성
        String userNickname;
        if ("kakao".equals(registrationId)) {
            userNickname = generateKakaoNickname();
        } else if ("google".equals(registrationId)) {
            userNickname = generateUserNickname();
        } else {
            throw new OAuth2AuthenticationException("Unsupported social media login");
        }

        UserAccountEntity newUserEntity = UserAccountEntity.builder()
                .userEmail(userEmail)
                .userName(userName)
                .userNickname(userNickname)
                .userPassword(passwordEncoder.encode(UUID.randomUUID().toString()))
                .build();

        newUserEntity.addUserRole(UserRoleType.SOCIAL);

        // streamingKey 생성 및 설정
        String streamingKey = generateStreamingKey();
        newUserEntity.updateUserStreamingKey(streamingKey);

        // 사용자 계정 저장
        userAccountRepository.save(newUserEntity);

        // 새 채널을 위한 카테고리 조회 또는 생성
        CategoryEntity categoryEntity = categoryRepository.findById(115L)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        // 채널 생성 및 사용자 계정과 연결
        ChannelEntity channelEntity = createChannel(newUserEntity, categoryEntity);
        channelEntity.setStreamingState(StreamingState.OFF);
        channelEntity.setChatState(ChatState.DEFAULT);

        // 채널 저장
        channelRepository.save(channelEntity);

        return newUserEntity;
    }

    /**
     * 사용자와 카테고리에 따라 채널을 생성.
     * 
     * @param userEntity      사용자 엔터티
     * @param categoryEntity  카테고리 엔터티
     * @return 생성된 채널 엔터티
     */
    private ChannelEntity createChannel(UserAccountEntity userEntity, CategoryEntity categoryEntity) {
        if (userEntity == null || userEntity.getUserNickname() == null) {
            throw new RuntimeException("사용자 정보가 올바르지 않습니다.");
        }
        return ChannelEntity.builder()
                .channelUserAccountEntity(userEntity)
                .categoryEntityOfChannel(categoryEntity)
                .channelContent("환영합니다! 새로운 채널입니다.")
                .channelCreatedTime(LocalDateTime.now())
                .channelProfileImg("default_profile.png")  // 프로필 이미지 추가
                .channelTitle(userEntity.getUserNickname() + "님의 채널")
                .channelViewerCount(0L)
                .build();
     
    }

    /**
     * 랜덤한 스트리밍 키를 생성.
     * 
     * @return 생성된 스트리밍 키
     */
    private String generateStreamingKey() {
        return UUID.randomUUID().toString();
    }
    
    public static String generateUserNickname() {
        // "Google"와 랜덤한 숫자 조합
        String prefix = "Google";
        int randomNumber = generateRandomNumber();
        String userNickname = prefix + randomNumber;

        return userNickname;
    }
    public static String generateKakaoNickname() {
    	// "Google"와 랜덤한 숫자 조합
    	String prefix = "Kakao";
    	int randomNumber = generateRandomNumber();
    	String userNickname = prefix + randomNumber;
    	
    	return userNickname;
    }

    private static int generateRandomNumber() {
        // 1000부터 9999까지의 랜덤한 숫자 생성
        Random random = new Random();
        return random.nextInt(9000) + 1000;
    }

}
