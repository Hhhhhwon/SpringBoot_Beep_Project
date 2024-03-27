package com.itwill.beep.domain;

public enum ChatState {
    DEFAULT("DEFAULT"),
    FOLLOW("FOLLOW"),
    ANONYMOUS("ANONYMOUS"),
    STREAMER_ONLY("STREAMER_ONLY");
    
    private String chatState;
    
    ChatState(String chatState) {
        this.chatState = chatState;
    }
    
    public String getChatstate() {
        return this.chatState;
    }
}
