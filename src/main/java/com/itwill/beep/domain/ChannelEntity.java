package com.itwill.beep.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.CreatedDate;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
@Table(name = "channels")
public class ChannelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 열시퀸스
    @Column(name = "channel_id")
    private Long channelId;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "channel_user_account")
    private UserAccountEntity channelUserAccountEntity;

    @Column(name = "channel_title")
    private String channelTitle;

    @Column(name = "channel_content")
    private String channelContent;

    @CreatedDate
    @Column(name = "channel_created_time")
    private LocalDateTime channelCreatedTime;

    @Column(name = "channel_profile_img")
    private String channelProfileImg;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_of_channel")
    private CategoryEntity categoryEntityOfChannel;

    @Column(name = "channel_viewer_count")
    private Long channelViewerCount;

    // 빌더패턴을 이용해서 객체를 생성할 때 nullPointExeption이 발생하는 것을
    // 방지하기 위해 비어있는 Set<>을 생성하는 에너테이션
    @Builder.Default
    // @ToString.Exclude
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "streaming_state", joinColumns = @JoinColumn(name = "channel_id"))
    // 애너테이션 안에서 애너테이션을 한 번 더 사용할 수 있다는 놀라운 사실!
    // 쉽게 설명해서 @CollectionTable 가 조인할 테이블의 이름을 설정하고
    // 애너테이션 안에 @JoinColumn을 사용하여 설정한 테이블의 설정한 컬럼과 조인하도록 만든 것이다.
    private Set<StreamingState> streamingStateSet = new HashSet<>();

    public ChannelEntity setStreamingState(StreamingState streamingState) {
        streamingStateSet.clear();
        streamingStateSet.add(streamingState);
        return this;
    }

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "chat_state", joinColumns = @JoinColumn(name = "channel_id"))
    private Set<ChatState> chatStateSet = new HashSet<>();

    public ChannelEntity setChatState(ChatState chatState) {
        chatStateSet.clear();
        chatStateSet.add(chatState);
        return this;
    }

    /* 방송 OFF시 시청자 수 조정하는 메서드 추가헀습니다. 확인必 */
    public void resetTotalViewerCount(Long channelViewerCount) {
        this.channelViewerCount = channelViewerCount;
    }

    public void updateChannelProfileImg(String channelProfileImg) {
        this.channelProfileImg = channelProfileImg;
    }

}
