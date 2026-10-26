package com.project.mst.module.auth.dto;

import java.util.List;
import java.util.UUID;

import com.project.mst.infra.security.JwtService;
import com.project.mst.module.users.UserModel;

public record UserResponse(UUID id, String name, String email, String avatarUrl, List<String> roles) {

    public static UserResponse from(UserModel user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAvatar_url(),
            JwtService.roles(user));
    }
}
