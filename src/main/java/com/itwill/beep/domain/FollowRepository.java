package com.itwill.beep.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<FollowEntity, Long> {
    // 팔로우 관계를 삭제하는 메서드
    void deleteByFollowerUserAccountAndFollowingUserAccount(UserAccountEntity followerUserAccount,
            UserAccountEntity followingUserAccount);

    // 특정 팔로워와 팔로잉 사이의 팔로우 관계를 조회하는 메서드
    FollowEntity findByFollowerUserAccountAndFollowingUserAccount(
            UserAccountEntity followerUserAccount, UserAccountEntity followingUserAccount);

    // 특정 사용자를 따르는 팔로워 수를 조회하는 메서드
    Long countByFollowingUserAccount(UserAccountEntity userAccount);

    // 특정 사용자의 팔로워 수를 조회하는 메서드
    Long countByFollowerUserAccount(UserAccountEntity userAccount);

    // 특정 사용자가 팔로우하는 사용자 목록을 조회하는 메소드
    List<FollowEntity> findByFollowerUserAccount(UserAccountEntity followerUserAccount);

    // 특정 사용자를 팔로우하는 사용자 목록을 조회하는 메소드
    List<FollowEntity> findByFollowingUserAccount(UserAccountEntity followingUserAccount);

    // 팔로워와 팔로잉 사용자 간의 관계가 존재하는지 확인하는 메소드
    boolean existsByFollowerUserAccountAndFollowingUserAccount(
            UserAccountEntity followerUserAccount, UserAccountEntity followingUserAccount);

    List<FollowEntity> findByFollowingUserAccountUserName(String followerUserAccountUserName);

}
