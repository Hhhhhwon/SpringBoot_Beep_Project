package com.itwill.beep.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.itwill.beep.domain.FollowEntity;
import com.itwill.beep.domain.FollowRepository;
import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.dto.FollowerListRequestDto;
import com.itwill.beep.dto.FollowerSearchRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class FollowService {
    private final FollowRepository followRepository;

    // 사용자를 팔로우하는 메서드
    @Transactional
    public void follow(UserAccountEntity followerUserAccount,
            UserAccountEntity followingUserAccount) {
        // 이미 팔로우 중인지 확인
        if (!followRepository.existsByFollowerUserAccountAndFollowingUserAccount(
                followerUserAccount, followingUserAccount)) {
            // 팔로우 관계를 저장
            FollowEntity followEntity =
                    FollowEntity.builder().followerUserAccount(followerUserAccount)
                            .followingUserAccount(followingUserAccount)
                            .createdTime(LocalDateTime.now()).build();
            followRepository.save(followEntity);
            log.info("User {} followed user {}", followerUserAccount.getUserName(),
                    followingUserAccount.getUserName());
        } else {
            log.warn("User {} is already following user {}", followerUserAccount.getUserName(),
                    followingUserAccount.getUserName());
        }

    }

    // 사용자의 팔로우를 취소하는 메서드
    @Transactional
    public void unfollow(UserAccountEntity followerUserAccount,
            UserAccountEntity followingUserAccount) {
        followRepository.deleteByFollowerUserAccountAndFollowingUserAccount(followerUserAccount,
                followingUserAccount);
    }

    // 특정 사용자를 팔로우하는지 여부를 확인하는 메서드
    public boolean isFollowing(UserAccountEntity followerUserAccount,
            UserAccountEntity followingUserAccount) {
        try {
            return followRepository.existsByFollowerUserAccountAndFollowingUserAccount(
                    followerUserAccount, followingUserAccount);
        } catch (Exception e) {
            log.error("Error occurred while checking if user {} is following user {}: {}",
                    followerUserAccount.getUserName(), followingUserAccount.getUserName(),
                    e.getMessage());
            return false;
        }
    }

    // 사용자의 팔로잉 목록을 가져오는 메서드
    public List<FollowEntity> getFollowings(UserAccountEntity userAccount) {
        try {
            return followRepository.findByFollowerUserAccount(userAccount);
        } catch (Exception e) {
            log.error("Error occurred while fetching followings for user {}: {}",
                    userAccount.getUserName(), e.getMessage());
            return null;
        }
    }

    // 사용자의 팔로워 목록을 가져오는 메서드
    public List<FollowEntity> getFollowers(UserAccountEntity userAccount) {
        try {
            return followRepository.findByFollowingUserAccount(userAccount);
        } catch (Exception e) {
            log.error("Error occurred while fetching followers for user {}: {}",
                    userAccount.getUserName(), e.getMessage());
            return null;
        }
    }

    // 사용자의 팔로잉 수를 계산하는 메서드
    public long countFollowings(UserAccountEntity userAccount) {
        try {
            return followRepository.countByFollowerUserAccount(userAccount);
        } catch (Exception e) {
            log.error("Error occurred while counting followings for user {}: {}",
                    userAccount.getUserName(), e.getMessage());
            return 0;
        }
    }

    // 사용자의 팔로워 수를 계산하는 메서드
    public long countFollowers(UserAccountEntity userAccount) {
        try {
            return followRepository.countByFollowingUserAccount(userAccount);
        } catch (Exception e) {
            log.error("Error occurred while counting followers for user {}: {}",
                    userAccount.getUserName(), e.getMessage());
            return 0;
        }
    }

    public Page<FollowerListRequestDto> followingList(String followingUserAccountUserName,
            int page) {
        log.info("followingList(followerUserAccountUserNickname={}, page={})",
                followingUserAccountUserName, page);

        Pageable pageable = PageRequest.of(page, 3, Sort.by("createdTime").descending());

        List<FollowEntity> followerPage =
                followRepository.findByFollowingUserAccountUserName(followingUserAccountUserName);

        log.info("팔로우페이지={}", followerPage.size());

        LocalDateTime now = LocalDateTime.now();

        List<FollowerListRequestDto> result = new ArrayList<>();
        for (FollowEntity entity : followerPage) {
            FollowerListRequestDto followerListRequestDto = FollowerListRequestDto.builder()
                    .followerUserAccountUserNickname(
                            entity.getFollowerUserAccount().getUserNickname())
                    .followerUserAcoountUserProfileImageUrl(
                            entity.getFollowerUserAccount().getUserProfileImageUrl())
                    .createdTime(entity.getCreatedTime())
                    .betweenCreatedTimeToNow(ChronoUnit.DAYS.between(entity.getCreatedTime(), now))
                    .build();
            result.add(followerListRequestDto);
        }

        return new PageImpl<>(result, pageable, result.size());
    }

    public Page<FollowerListRequestDto> search(FollowerSearchRequestDto dto) {
        log.info("search(dto={})", dto);

        List<FollowEntity> followerSearchResult = followRepository
                .findByFollowingUserAccountUserName(dto.getFollowingUserAccountUserName());
        LocalDateTime now = LocalDateTime.now();

        List<FollowerListRequestDto> result = new ArrayList<>();
        for (FollowEntity entity : followerSearchResult) {
            if (entity.getFollowerUserAccount().getUserNickname().toLowerCase()
                    .contains(dto.getKeyword().toLowerCase())) {
                FollowerListRequestDto followerListRequestDto =
                        FollowerListRequestDto.builder()
                                .followerUserAccountUserNickname(
                                        entity.getFollowerUserAccount().getUserNickname())
                                .followerUserAcoountUserProfileImageUrl(
                                        entity.getFollowerUserAccount().getUserProfileImageUrl())
                                .createdTime(entity.getCreatedTime())
                                .betweenCreatedTimeToNow(
                                        ChronoUnit.DAYS.between(entity.getCreatedTime(), now))
                                .build();
                result.add(followerListRequestDto);
            }
        }
        Pageable pageable = PageRequest.of(dto.getP(), 3, Sort.by("createdTime").descending());

        return new PageImpl<>(result, pageable, result.size());
    }

}
