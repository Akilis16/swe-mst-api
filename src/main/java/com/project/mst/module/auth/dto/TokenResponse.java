package com.project.mst.module.auth.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresIn, UserResponse user) {

    public static TokenResponse bearer(String token, long expiresIn, UserResponse user) {
        return new TokenResponse(token, "Bearer", expiresIn, user);
    }
}
