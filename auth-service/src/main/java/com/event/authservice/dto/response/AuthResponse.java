package com.event.authservice.dto.response;

import java.util.List;

public class AuthResponse {

    private final String accessToken;
    private final String tokenType;
    private final Long userId;
    private final String email;
    private final List<String> roles;

    public AuthResponse(String accessToken, String tokenType, Long userId, String email, List<String> roles) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.userId = userId;
        this.email = email;
        this.roles = roles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }
}
