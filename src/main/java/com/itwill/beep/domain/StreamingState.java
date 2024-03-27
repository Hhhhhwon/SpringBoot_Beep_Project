package com.itwill.beep.domain;

public enum StreamingState {
    ON("ON"),
    OFF("OFF");
    
    private String streamingState;

    StreamingState(String streamingState) {
        this.streamingState = streamingState;
    }
    
    public String getStreamingstate() {
        return this.streamingState;
    }
}
