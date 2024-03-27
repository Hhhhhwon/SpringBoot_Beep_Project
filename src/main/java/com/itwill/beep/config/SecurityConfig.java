package com.itwill.beep.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.itwill.beep.domain.CustomOAuth2UserService;

@Configuration // 설정파일임을 스프링컨테이너에게 알려주는 애너테이션
@EnableMethodSecurity // -> 스프링 세큐리티 메서드 활성화
@EnableWebSecurity
public class SecurityConfig {
	
	
	 private final CustomOAuth2UserService customOAuth2UserService;

	    public SecurityConfig(@Lazy CustomOAuth2UserService customOAuth2UserService) {
	        this.customOAuth2UserService = customOAuth2UserService;
	    }

	 
	
	
		

    @Bean // 의존성 주입을 위한 객체설정
    // Spring Security 5 버전부터 비밀번호는 반드시 암호화를 해야 함.
    // 비밀번호를 암호화하지 않으면 HTTP 403(access denied, 접근 거부) 또는
    // HTTP 500 (내부 서버 오류, internal server error) 에러가 발생함.
    // 비밀번호 암호화를 할 수 있는 객체를 스프링 컨테이너가 bean으로 관리해야 함.
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // 암호화 메서드
    }
    
    // 스프링 시큐리티 필터 체인 객체(bean):
    // 로그인/로그아웃 관련 설정.
    // 로그인 페이지(뷰), 로그아웃 페이지(뷰) 설정.
    // 페이지 접근 권한, 인증 설정.(로그인 없이 접근 가능한 페이지/로그인해야만 접근 가능한 페이지)
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	
    	  // CSRF 보호 비활성화
        http.csrf(csrf -> csrf.disable())
            // 폼 로그인 설정
            .formLogin(login -> login
                .loginPage("/user/login")
            )
            // 로그아웃 설정 및 로그아웃 성공 시 리다이렉트 URL을 "/"로 지정
            .logout(logout -> logout
                .logoutSuccessUrl("/")
            )
            // OAuth2 로그인 설정
            .oauth2Login(oauth2Login -> oauth2Login
            		  // 5. 사용자 정보 엔드포인트 설정
                    // userInfoEndpoint:
                    //   - OAuth2 로그인 이후 사용자 정보 엔드포인트 설정
                    //   - 사용자 정보를 가져오는데 사용되는 서비스를 등록.
                    .userInfoEndpoint(userInfo -> userInfo
                        // 6. 사용자 정보를 가져오는 서비스 설정
                        // CustomOAuth2UserService:
                        //   - OAuth2 로그인 이후 사용자 정보를 가져오기 위한 커스텀 서비스 클래스
                        //   - loadUser 메서드를 오버라이드하여 사용자 정보를 처리하고 반환
                        .userService(customOAuth2UserService)
                    
                )
            );
        
    	return http.build();
    	
       
    }
}
