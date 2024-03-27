package com.itwill.beep.dto;

import com.itwill.beep.domain.CategoryEntity;
import com.itwill.beep.domain.ChannelEntity;

import lombok.Data;

@Data
public class StreamingOnDto {

    private Long channelId;
    private String title;
    private String content;
    private Long categoryId;
    
    public ChannelEntity toEntity() {
        return ChannelEntity.builder()
                .channelId(channelId)
                .channelTitle(title)
                .channelContent(content)
                .categoryEntityOfChannel(CategoryEntity.builder().categoryId(categoryId).build())
                .build();
    }
}
