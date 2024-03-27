package com.itwill.beep.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.NaturalId;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Basic;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "user_accounts") // 원래 users -> user_accounts로 변경
public class UserAccountEntity{ // -> 원래 Account -> UserAccountEntity 로 변경
	
	
	

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 열시퀸스
    @Column(name = "user_id") // 원래 user_no -> user_id 변경
    private Long userId; // 원래 id -> userId 변경

    @EqualsAndHashCode.Include // username 필드 만으로 equals 비교를 하기 위해서.
    @NaturalId(mutable = true) // 여기서 mutable=true를 추가하여 변경 가능하게 설정
    @Basic(optional = false)
    @Column(name = "user_name") // set절에서 제외
    // 원래 user_id -> user_name 변경
    private String userName; // username -> userName 변경(security의 username과 구분하기 위함)

    @Basic(optional = false)
    @Column(name = "user_password")
    private String userPassword; // 원래 user_password -> userPassword 변경

    @Basic(optional = false)
    @Column(name = "user_nickname") // 원래 username -> user_nickname 변경
    private String userNickname;

    @Basic(optional = false)
    @Column(name = "user_email")
    private String userEmail;

    @Basic(optional = false)
    @Column(name = "user_streaming_key")
    private String userStreamingKey;

    @Column(name = "user_profile_image_url")
    private String userProfileImageUrl;

    @Column(length = 300, name = "user_self_introduction")
    private String userSelfIntroduction; // 자기 소개

    @Column(name = "user_email_verified")
    private boolean userEmailVerified;

    // User와 StreamingInfo 사이의 1:N 관계 설정
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StreamingInfo> streamingInfos;







    // 빌더패턴을 이용해서 객체를 생성할 때 nullPointExeption이 발생하는 것을
    // 방지하기 위해 비어있는 Set<>을 생성하는 에너테이션
    @Builder.Default
    @ToString.Exclude
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    // 애너테이션 안에서 애너테이션을 한 번 더 사용할 수 있다는 놀라운 사실!
    // 쉽게 설명해서 @CollectionTable 가 조인할 테이블의 이름을 설정하고
    // 애너테이션 안에 @JoinColumn(name = "user_id")을 사용하여 설정한 테이블의 설정한 컬럼과 조인하도록 만든 것이다.
    private Set<UserRoleType> userRoles = new HashSet<>();

    public UserAccountEntity addUserRole(UserRoleType role) {
        userRoles.add(role);
        return this;
    }

    public UserAccountEntity clearUserRoles() {
        userRoles.clear();
        return this;
    }

    public void updateUserName(String newUserName){
        this.userName = newUserName;
    }

    public void updateUserPassword(String newUserPassword){
        this.userPassword = newUserPassword;
    }
    public void updateUserStreamingKey(String newUserStreamingKey) {
        this.userStreamingKey = newUserStreamingKey;
    }
    
    
    // 비밀번호 변경
    public void updateUserPassword(String newpassword, PasswordEncoder passwordEncoder) {
    this.userPassword = passwordEncoder.encode(newpassword);

       
        
    }
    
 

    public void updateUserNickname(String newUserNickname){
        this.userNickname = newUserNickname;
    }

    public void updateUserSelfIntroduction(String newUserSelfIntroduction){
        this.userSelfIntroduction = newUserSelfIntroduction;
    }

    // 이메일 인증 여부를 설정하는 메소드
    public void updateEmailVerified(boolean NewEmailVerified) {
        this.userEmailVerified = NewEmailVerified;
    }

}
