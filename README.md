# bangbang

일정 >>
1월 23일 - 26일(늦어도 1월 말까지) : 기획 + DB
2월 1일 - 2월 25일 : 개발 진행.(+AWS 교체)
2월 26일 - 2월 말 : 리팩토링
3월 1일 - 3월 11일 : 리팩토링 + 발표준비

주제 선정 - 기획DB - 개발(프+백)- 리팩토링 -발표준비

규칙 >>
<<프로젝트 규칙>>
--------------
<커뮤니케이션>
카톡에 오전시작-오후시작-종료 시(오후 6시), (오늘 할 것 - 오늘 한 것 - 앞으로 할 것) 공유
일과 시작 시, 카톡에 오늘 할 것 업로드
오후 6시, 카톡에 오늘 한 것 + 내일 할 것 업로드 
--------------
<git 관련>
git 올리려고 할 때, 카톡에 공고
(ex: 누구누구 깃 올릴게요~) 
git 올린 후, 카톡에 git 내용 공유
---------------
<코드 중복-충돌>
해당 팀원에게 문의한 후에 중복-충돌 코드 수정하기.
------------
<코드 정리-리팩토링>
*코드 정리한 후에 github 업로드(최소한 gpt에게 코드 정리 관련 문의해서 해결한 후에 업로드하기)
*주석, 로그 꼼꼼히 짜기.
*fix된 기능은 관여된 팀원과 상의 하에 리팩토링 진행.
(여유 있을 때 리팩토링. 기능 구현 선순위) 
-----------------

<1월 23일> 
오후 3시 - 4시 30분
팀 구성 및 회의 진행

4시 30분 - 6시 30분
주제 조사

---


# Spring Boot JPA 구글 소셜로그인 🌐 (코드는 프로젝트 완료 AWS 배포 후 올릴예정)

# 프로젝트 개요
### 이 예제는 Spring Boot와 Timeleaf, JPA를 사용하여 구글 소셜 로그인을 구현한 예제입니다. 사용자 정보를 OAuth 2.0을 이용하여 획득하고 데이터베이스에 저장하며, 소셜 로그인 버튼을 통해 간편한 회원가입을 제공.

구글 소셜 로그인 버튼  🌟

![alt text](https://github.com/Hhhhhwon/spring-boot-JPA/blob/main/image/image.png?raw=true)


![alt text](https://github.com/Hhhhhwon/spring-boot-JPA/blob/main/image/%EA%B5%AC%EA%B8%80%20%EB%A1%9C%EA%B7%B8%EC%9D%B8%201.png?raw=true)

``` java
 div class="social">
    <a href="/oauth2/authorization/google" class="social-button google">
        <img src="/images/google.png" alt="Google Login">
    </a>
</div>
 
```


![alt text](https://github.com/Hhhhhwon/spring-boot-JPA/blob/main/image/%EA%B5%AC%EA%B8%80%20%EB%A1%9C%EA%B7%B8%EC%9D%B8%202.png?raw=true)


# (임시)닉네임 생성 로직 🎭
![alt text](https://github.com/Hhhhhwon/spring-boot-JPA/blob/main/image/%EA%B5%AC%EA%B8%80%EB%A1%9C%EA%B7%B8%EC%9D%B8%203.png?raw=true)
```java
  public static String generateUserNickname() {
        // "Google"와 랜덤한 숫자 조합
        String prefix = "Google";
        int randomNumber = generateRandomNumber();
        String userNickname = prefix + randomNumber;

        return userNickname;
    }
```


## DB에 정보 저장 확인 💾

![alt text](https://github.com/Hhhhhwon/spring-boot-JPA/blob/main/image/%EA%B5%AC%EA%B8%80%20%EB%A1%9C%EA%B7%B8%EC%9D%B8%204.png?raw=true)


## 사용자 권한 부여 🛡️
![alt text](https://github.com/Hhhhhwon/spring-boot-JPA/blob/main/image/%EA%B5%AC%EA%B8%80%20%EB%A1%9C%EA%B7%B8%EC%9D%B8%205.png?raw=true)
- 권한은 USER_SOCIAL로 부여(나중에 시간이 된다면 로직부터 바꾸고  kakao,google,naver 로 나눌예정)

## 🚀 OAuth2 유저 , 일반 로그인 유저
- principal 객체의 유형을 확인하여 OAuth2로 로그인한 사용자와 Form 로그인한 사용자를 구분하여 처리
```java
   // (3) OAuth2User 또는 UserDetails에 따라 처리
        if (principal instanceof OAuth2User) {
            handleOAuth2User((OAuth2User) principal, model);
        } else if (principal instanceof UserDetails) {
            handleUserDetails((UserDetails) principal, model);
        } else {
            log.info("---> 누구세요..?");
        }
```

```java

// OAuth2User 정보:

Name: [112931286020696657081], Granted Authorities: [[ROLE_SOCIAL]], User Attributes: [{sub=112931286020696657081, name=ㅇㅇㅇ, given_name=ㅇㅇ, family_name=ㅇ, picture=https://lh3.googleusercontent.com/a/ACg8ocJqY0Yde7S-GKpek4ZyLINtjShMJXg93HPOjQ=s96-c, email=llll1111@gmail.com, email_verified=true, locale=ko}]

User Attributes에는 사용자의 구글 정보가 포함되어 있다. (name, given_name, family_name, picture, email 등)

// UserDetails 정보:

--- home() principal=com.itwill.beep.dto.UserSecurityDto [Username=a, Password=[PROTECTED], Enabled=true, AccountNonExpired=true, CredentialsNonExpired=true, AccountNonLocked=true, Granted Authorities=[ROLE_USER]]

일반 Form 로그인을 통해 얻은 사용자 정보.
UserSecurityDto는 Spring Security의 UserDetails 인터페이스를 구현한 객체로, 일반적으로 사용자의 인증 및 권한 정보를 담고 있다.

각 principal은 다른 인증 방식으로 인해 생성되고, 서비스 내에서 사용자의 권한과 정보를 다르게 처리. Spring Security 에서 다양한 인증 방식을 지원하여 유연한 사용자 관리를 가능케 하는 특징 중 하나이다


```

## OAth2 느낀점 🚀
 팀원이 Spring Security를 이용한 기본 회원가입 로직과 다른 Controller에 연결되는 코드들이  작성되어 있었다. 해당 코드를 처음 접하면서 이해하는 데에 시간이 조금 걸렸고. 다른 사람의 코드를 많이 봐야겠다는 생각이 들었다..
```java

// 이코드로 유저의 해당정보를 갖고와서 회원가입을 진행시키는 로직이었는데
// OAuth2User에서는 저코드를 대입시키면 고유식별자 11293128602069665708 이라는숫자만
// 나온다 여기서 쪼끔 헤맸던거같다 
SecurityContextHolder.getContext().getAuthentication().getName();


 // 소셜로그인 할때 DB 안에 내이름을 아이디로 저장하였기 떄문에 이코드로 "name"을 추출하면 그게 소셜 쪽아이디다 
 // 소셜 로그인 시 사용자의 이름을 가져오는 코드
 String oauth2name = (String) ((OAuth2User) principal).getAttributes().get("name");

 // 이 코드는 어떤 OAuth2 제공자를 통해 로그인되었는지 알 수 있고, 그에 따라 분기 처리를 할 수 있다(나중에 kakao,naver).
String registrationId = userRequest.getClientRegistration()getRegistrationId();

```





# 프로젝트 진행 상황 및 향후 계획 🚀
  #### 프로젝트 중간에 소셜 로그인 기능을 담당하여  구글 로그인을 구현하였습니다. 진행중인 프로젝트 내에서의 우선순위가 다른 기능들로 인해서 , 카카오와 네이버 소셜 로그인 구현 계획을 잠시 미뤄두었습니다. 현재 급한 기능들을 우선으로 진행 중이며, 나중에 프로젝트의 특정 단계나 개인 프로젝트로 카카오와 네이버 소셜 로그인을 추가할 계획입니다!🚀












