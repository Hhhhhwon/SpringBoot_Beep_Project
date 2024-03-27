package com.itwill.beep.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FollowerListRequestDto {
    private String followerUserAccountUserNickname;
    private String followerUserAcoountUserProfileImageUrl;
    private LocalDateTime createdTime;
    private Long betweenCreatedTimeToNow;
}
