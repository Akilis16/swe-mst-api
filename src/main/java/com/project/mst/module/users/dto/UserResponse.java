package com.project.mst.module.users.dto;

import java.util.List;
import java.util.UUID;

import com.project.mst.infra.security.JwtService;
import com.project.mst.module.users.UserModel;

public record UserResponse(UUID id, String displayName, String email, List<String> roles, Preferences preferences) {

    public static UserResponse from(UserModel user) {
        return new UserResponse(
            user.getId(),
            user.getDisplayName(),
            user.getEmail(),
            JwtService.roles(user),
            Preferences.fromJson(user.getPreferences()));
    }
}
