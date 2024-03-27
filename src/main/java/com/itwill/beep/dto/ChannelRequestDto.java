package com.itwill.beep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChannelRequestDto {
    private String channelUserAccountEntityChannelName;
    private String channelTitle;
    private String channelProfileImg;
    private String categoryEntityOfChannelCategoryName;
    private Long channelViewerCount;
    private String streamingState;

}
