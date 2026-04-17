package com.event.authservice.dto.response;

import java.util.List;

public class TokenValidationResponse {

    private final boolean valid;
    private final Long userId;
    private final String email;
    private final List<String> roles;

    public TokenValidationResponse(boolean valid, Long userId, String email, List<String> roles) {
        this.valid = valid;
        this.userId = userId;
        this.email = email;
        this.roles = roles;
    }

    public boolean isValid() {
        return valid;
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
