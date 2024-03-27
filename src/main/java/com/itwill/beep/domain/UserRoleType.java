package com.itwill.beep.domain;

public enum UserRoleType {
    USER("ROLE_USER"),
    STREAMER("ROLE_STREAMER"),
    ADMIN("ROLE_ADMIN"),
	SOCIAL("ROLE_SOCIAL");





    
    private String authority;
    
    UserRoleType(String authority) {
        this.authority = authority;
    }
    
    public String getAuthority() {
        return this.authority;
    }

}
