package com.itwill.beep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BeepSearchRequestDto {
    private String userNickname;
    private String channelTitle;
    private String categoryName;
    private String keyword;
}
