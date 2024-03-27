package com.itwill.beep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FollowerSearchRequestDto {
    private String followingUserAccountUserName;
    private String keyword;
    private int p;
}
