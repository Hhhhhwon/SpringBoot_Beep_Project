package com.itwill.beep.dto;

public class StreamingStartRequestDto {
    private String streamingKey; // 스트리밍을 식별하는 키

    // 기본 생성자
    public StreamingStartRequestDto() {
    }

    // streamKey 필드에 대한 getter와 setter
    public String getStreamingKey() {
        return streamingKey;
    }

    public void setStreamingKey(String streamingKey) {
        this.streamingKey = streamingKey;
    }
}
