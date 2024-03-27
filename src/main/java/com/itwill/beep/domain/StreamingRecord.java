package com.itwill.beep.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "streaming_records")
public class StreamingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long streamingId;

    private String streamingKey;
    private LocalDateTime streamingStartTime;

    public void updateStreamingKey(String newStreamingKey){
        this.streamingKey = newStreamingKey;
    }

    public void updateStreamingStartTime(LocalDateTime newStreamingStartTime){
        this.streamingStartTime = newStreamingStartTime;
    }

}
