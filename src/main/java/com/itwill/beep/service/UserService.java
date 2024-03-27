package com.itwill.beep.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.itwill.beep.domain.CategoryEntity;
import com.itwill.beep.domain.CategoryRepository;
import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.ChannelRepository;
import com.itwill.beep.domain.ChatState;
import com.itwill.beep.domain.StreamingState;
import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.domain.UserAccountRepository;
import com.itwill.beep.domain.UserRoleType;
import com.itwill.beep.domain.VerificationToken;
import com.itwill.beep.dto.SignupRequestDto;
import com.itwill.beep.dto.UserSecurityDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userAccountRepository;
    private final ChannelRepository channelRepository; // ChannelEntity를 저장하기 위한 Repository
    private final CategoryRepository categoryRepository;

    /**
     * Spring Security의 UserDetailsService를 구현한 메소드. 주어진 사용자 이름으로 사용자를 조회하고, 해당 사용자가 존재할 경우
     * UserDetails로 변환하여 반환한다.
     *
     * @param userName 조회할 사용자 이름
     * @return UserDetails로 변환된 사용자 정보
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우 발생하는 예외
     */
    private final VerificationTokenService verificationTokenService;

    public boolean validateStreamKey(String key) {
        return userAccountRepository.existsByUserStreamingKey(key);
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        log.info("username = {}", userName);

        UserAccountEntity userAccountEntity = userAccountRepository.findByUserName(userName);
        if (userAccountEntity != null) {
            return UserSecurityDto.fromEntityToDto(userAccountEntity);
        } else {
            throw new UsernameNotFoundException(userName + "찾을 수 없음!");
        }
    }

    /**
     * 회원 가입을 처리하는 메소드.
     *
     * @param signupRequestDto 회원 가입 정보를 담고 있는 DTO
     */
    public void createUserAccount(SignupRequestDto signupRequestDto) {
        // DTO를 UserAccountEntity 객체로 변환
        UserAccountEntity userEntity = signupRequestDto.toEntity(passwordEncoder);

        // 역할 설정
        userEntity.addUserRole(UserRoleType.USER);

        // streamingKey 생성 및 설정
        String streamingKey = generateStreamingKey();
        userEntity.updateUserStreamingKey(streamingKey);

        // 사용자 계정 저장
        userAccountRepository.save(userEntity);

        // 새 채널을 위한 카테고리 조회 또는 생성
        CategoryEntity categoryEntity = categoryRepository.findById(115L) // 예시: ID가 1인 카테고리 조회
                .orElseThrow(() -> new RuntimeException("Category not found")); // 카테고리를 찾지 못한 경우 예외
                                                                                // 처리
        // 채널 생성 및 사용자 계정과 연결
        ChannelEntity channelEntity = ChannelEntity.builder().channelUserAccountEntity(userEntity)
                .categoryEntityOfChannel(categoryEntity) // 예시 카테고리
                .channelContent("Welcome to your new channel!") // 채널 설명 또는 초기 콘텐츠
                .channelCreatedTime(LocalDateTime.now()) // 생성 시간
                .channelProfileImg("/images/default-profile.png") // 기본 프로필 이미지 경로
                .channelTitle(signupRequestDto.getUserName() + "'s Channel") // 채널 제목
                .channelViewerCount(0L) // 초기 시청자 수
                .build();
        channelEntity.setStreamingState(StreamingState.OFF);
        channelEntity.setChatState(ChatState.DEFAULT);

        // 채널 저장
        channelRepository.save(channelEntity);

        log.info("Created user account with streaming key and channel for user: {}",
                signupRequestDto.getUserName());
    }

    /**
     * 사용자 이름으로 사용자를 조회하는 메소드.
     *
     * @param userName 조회할 사용자 이름
     * @return 조회된 사용자 정보
     */
    public UserAccountEntity findUserByUserName(String userName) {
        return userAccountRepository.findByUserName(userName);
    }

    /**
     * 사용자 닉네임으로 사용자를 조회하는 메소드.
     *
     * @param userNickname 조회할 사용자 닉네임
     * @return 조회된 사용자 정보
     */
    public UserAccountEntity findUserByUserNickname(String userNickname) {
        return userAccountRepository.findByUserNickname(userNickname);
    }

    /**
     * 사용자 ID로 사용자를 조회하는 메소드.
     *
     * @param userId 조회할 사용자 ID
     * @return 조회된 사용자 정보
     */
    public UserAccountEntity findByUserId(Long userId) {
        return userAccountRepository.findByUserId(userId);
    }

    /**
     * 사용자 이름이 존재하는지 여부를 확인하는 메소드.
     *
     * @param userName 확인할 사용자 이름
     * @return 사용자 이름이 존재하면 true, 그렇지 않으면 false
     */
    public boolean isUserNameExists(String userName) {
        return userAccountRepository.existsByUserName(userName);
    }

    /**
     * 사용자 이름이 존재하는지 여부를 확인하는 메소드.
     *
     * @param userNickname 확인할 사용자 이름
     * @return 사용자 이름이 존재하면 true, 그렇지 않으면 false
     */
    public boolean isUserNicknameExists(String userNickname) {
        return userAccountRepository.existsByUserNickname(userNickname);
    }

    /**
     * 사용자 이메일이 존재하는지 여부를 확인하는 메소드.
     *
     * @param userEmail 확인할 사용자 이름
     * @return 사용자 이름이 존재하면 true, 그렇지 않으면 false
     */
    public boolean isUserEmailExists(String userEmail) {
        return userAccountRepository.existsByUserEmail(userEmail);
    }

    @Transactional
    public void updateUserPassword(String username, String newPassword) {
        // 사용자 아이디로 사용자를 찾음
        UserAccountEntity userAccountEntity = userAccountRepository.findByUserName(username);

        if (userAccountEntity != null) {
            // 새로운 비밀번호로 업데이트
            userAccountEntity.updateUserPassword(newPassword, passwordEncoder);
            // 변경된 정보를 저장
            userAccountRepository.save(userAccountEntity);
        } else {
            throw new IllegalArgumentException("해당 아이디에 해당하는 사용자를 찾을 수 없습니다.");
        }
    }

    public Optional<UserAccountEntity> findByUserEmail(String email) {
        return userAccountRepository.findByUserEmail(email);
    }



    public String generateStreamingKey() {
        // 단순히 UUID를 기반으로 streamingKey 생성
        return UUID.randomUUID().toString();
    }

    public String reGenerateStreamingKey(String userName) {
        UserAccountEntity userAccountEntity = userAccountRepository.findByUserName(userName);
        if (userAccountEntity == null) {
            // 사용자 계정을 찾을 수 없는 경우, 적절한 예외 처리 또는 로그를 추가
            log.error("사용자 계정을 찾을 수 없습니다. userName: {}", userName);
            return null; // 또는 적절한 예외를 던짐
        }

        String streamingKey = generateStreamingKey();
        userAccountEntity.updateUserStreamingKey(streamingKey);

        userAccountRepository.save(userAccountEntity);

        return streamingKey;
    }

    public boolean validateStreamingKey(String streamingKey) {
        try {
            boolean streamingKeyValid =
                    (userAccountRepository.findByUserStreamingKey(streamingKey) != null);
            log.info("Validating streaming key: {} - Result: {}", streamingKey, streamingKeyValid);
            return streamingKeyValid;
        } catch (Exception e) {
            log.error("Error validating streaming key: {}", streamingKey, e);
            throw e; // 예외 다시 던지기
        }
    }

    @Transactional
    public void updateProfile(String username, String nickname, String selfIntroduction) {
        // 사용자 정보 업데이트 로직 구현
        UserAccountEntity user = userAccountRepository.findByUserName(username);
        if (user == null) {
            throw new IllegalArgumentException("해당 사용자가 존재하지 않습니다. username=" + username);
        }
        user.updateUserNickname(nickname);
        user.updateUserSelfIntroduction(selfIntroduction);
    }

    public boolean checkIfUserEmailVerified(String username) {
        UserAccountEntity user = userAccountRepository.findByUserName(username);
        return user.isUserEmailVerified();
    }

    public void updateUsername(String currentUsername, String newUsername) {
        UserAccountEntity user = userAccountRepository.findByUserName(currentUsername);
        if (user != null && user.isUserEmailVerified()) {
            user.updateUserName(newUsername);
            userAccountRepository.save(user);
        }
    }

    // 이메일을 통해 사용자 아이디(이름)을 찾는 기능
    public String findUserNameByEmail(String email) {
        UserAccountEntity userAccount = userAccountRepository.findByUserEmail(email).orElseThrow();
        if (userAccount != null) {
            return userAccount.getUserName();
        } else {
            // 사용자가 없을 경우 적절한 예외 처리 또는 메시지 반환
            throw new UsernameNotFoundException("해당 이메일로 등록된 사용자를 찾을 수 없습니다.");
        }
    }

    // 토큰을 검증하고 새 비밀번호로 업데이트하는 로직
    public void resetPassword(String token, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        VerificationToken verificationToken = verificationTokenService.findByToken(token);
        if (verificationToken == null || verificationToken.isExpired()) {
            throw new RuntimeException("토큰이 유효하지 않거나 만료되었습니다.");
        }

        UserAccountEntity user = verificationToken.getVerifiTokenUserAccount();
        user.updateUserPassword(passwordEncoder.encode(password));
        userAccountRepository.save(user);
        verificationTokenService.invalidateToken(verificationToken);

        log.info("Password reset successfully for user: {}", user.getUserName());
    }

    /* 채널 이미지 변경 서비스 */


}
