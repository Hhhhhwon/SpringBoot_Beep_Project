package com.itwill.beep.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itwill.beep.domain.VerificationToken;
import com.itwill.beep.dto.SignupRequestDto;
import com.itwill.beep.service.EmailService;
import com.itwill.beep.service.UserService;
import com.itwill.beep.service.VerificationTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserAccountController {
    
    private final UserService userService;
    private final EmailService emailService;
    private final VerificationTokenService tokenService;
    
    @GetMapping("/login")
    public void userLogin() {
        log.info("userLogin()");
    }
    
    @GetMapping("/signup")
    public void userSignin () {
        log.info("userSignup()");
    }
    
    @PostMapping("/signup")
    public String createUserAccount(@ModelAttribute SignupRequestDto signupRequestDto) {
        log.info("signupRequestDto = {}", signupRequestDto);
        
        userService.createUserAccount(signupRequestDto);
        
        // 회원가입 이후 로그인 페이지로
        return "redirect:/user/login";
    }

    // 아이디 찾기 페이지로 이동하는 메서드
    @GetMapping("/find-username-page")
    public String showFindUsernamePage() {
        return "user/findUsernamePage";
    }

    // 비밀번호 찾기 페이지로 이동
    @GetMapping("/find-password-page")
    public String showFindPasswordPage() {
        return "user/findPasswordPage";
    }

    // 비밀번호 재설정 이메일 전송 요청 처리
    @PostMapping("/send-password-reset-email")
    public ResponseEntity<?> sendPasswordResetEmail(@RequestBody Map<String, String> payload) {
        try {
            emailService.sendPasswordResetEmail(payload.get("email"));
            return new ResponseEntity<>("비밀번호 재설정 이메일을 전송했습니다.", HttpStatus.OK);
        } catch (UsernameNotFoundException ex) {
            log.error("User not found", ex);
            return new ResponseEntity<>("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            log.error("Error during the send password reset email", ex);
            return new ResponseEntity<>("이메일 전송 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 비밀번호 재설정 페이지로 이동
    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam String token, Model model) {
        VerificationToken verificationToken = tokenService.findByToken(token);
        if (verificationToken == null || verificationToken.isExpired()) {
            model.addAttribute("error", "토큰이 유효하지 않거나 만료되었습니다.");
            return "errorPage"; // 적절한 에러 페이지로 리디렉션
        }
        model.addAttribute("token", token);
        return "user/resetPasswordPage";
    }

    // 비밀번호 재설정 요청 처리
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token,
        @RequestParam String password,
        @RequestParam String confirmPassword) {
        try {
            userService.resetPassword(token, password, confirmPassword);
            return new ResponseEntity<>("비밀번호가 성공적으로 변경되었습니다.", HttpStatus.OK);
        } catch (InvalidCsrfTokenException ex) {
            return new ResponseEntity<>("유효하지 않은 토큰입니다.", HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>("비밀번호 확인이 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("Error during the password reset", ex);
            return new ResponseEntity<>("비밀번호 재설정 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 회원가입 유효성 검사
     * 회원 가입 시, 클라이언트로부터 전달된 사용자 아이디(username)의 중복 여부를 확인하는 엔드포인트.
     * @param username 클라이언트가 전달한 사용자 아이디
     * @return 이미 존재하는 경우 "Y", 존재하지 않는 경우 "N"을 반환하는 ResponseEntity
     */
    @GetMapping("/signup/checkusername")
    @ResponseBody
    public ResponseEntity<String> checkUsername(@RequestParam(name = "username") String username) {
        // 현재 메서드의 실행 로그.
        log.debug("checkusername(username={})", username);

        // UserService를 통해 전달된 사용자 아이디가 이미 존재하는지 확인.
        boolean isUserIdExists = userService.isUserNameExists(username);
        
        // 현재 확인하고 있는 사용자 아이디 로그.
        log.info(username);

        // 존재하는 경우 "Y"를 반환.
        // 존재하지 않는 경우 "N"을 반환.
        return isUserIdExists
                ? ResponseEntity.ok("Y") // 아이디가 이미 존재하는 경우
                : ResponseEntity.ok("N"); // 아이디가 존재하지 않는 경우
    }
    
    /**
     * 회원가입 유효성 검사
     * 회원 가입 시, 클라이언트로부터 전달된 사용자 아이디(userNickname)의 중복 여부를 확인하는 엔드포인트.
     * @param userNickname 클라이언트가 전달한 사용자 아이디
     * @return 이미 존재하는 경우 "Y", 존재하지 않는 경우 "N"을 반환하는 ResponseEntity
     */
    @GetMapping("/signup/checkUsernickname")
    @ResponseBody
    public ResponseEntity<String> checkUsernickname(@RequestParam(name = "usernickname") String usernickname) {
        // 현재 메서드의 실행 로그.
        log.debug("checkUsernickname(usernickname={})", usernickname);

        // UserService를 통해 전달된 사용자 아이디가 이미 존재하는지 확인.
        boolean isUserNicknameExists = userService.isUserNicknameExists(usernickname);
        
        // 현재 확인하고 있는 사용자 아이디 로그.
        log.info(usernickname);

        // 존재하는 경우 "Y"를 반환.
        // 존재하지 않는 경우 "N"을 반환.
        return isUserNicknameExists
                ? ResponseEntity.ok("Y") // 아이디가 이미 존재하는 경우
                : ResponseEntity.ok("N"); // 아이디가 존재하지 않는 경우
    }
    
    /**
     * 회원가입 유효성 검사
     * 회원 가입 시, 클라이언트로부터 전달된 사용자 아이디(userEmail)의 중복 여부를 확인하는 엔드포인트.
     * @param userEmail 클라이언트가 전달한 사용자 아이디
     * @return 이미 존재하는 경우 "Y", 존재하지 않는 경우 "N"을 반환하는 ResponseEntity
     */
    @GetMapping("/signup/checkUseremail")
    @ResponseBody
    public ResponseEntity<String> checkUseremail(@RequestParam(name = "useremail") String useremail) {
    	// 현재 메서드의 실행 로그.
    	log.debug("checkUseremail(useremail={})", useremail);
    	
    	// UserService를 통해 전달된 사용자 아이디가 이미 존재하는지 확인.
    	boolean isUserEmailExists = userService.isUserEmailExists(useremail);
    	
    	// 현재 확인하고 있는 사용자 아이디 로그.
    	log.info(useremail);
    	
    	// 존재하는 경우 "Y"를 반환.
    	// 존재하지 않는 경우 "N"을 반환.
    	return isUserEmailExists
    			? ResponseEntity.ok("Y") // 아이디가 이미 존재하는 경우
    			: ResponseEntity.ok("N"); // 아이디가 존재하지 않는 경우
    }
    

    
}
