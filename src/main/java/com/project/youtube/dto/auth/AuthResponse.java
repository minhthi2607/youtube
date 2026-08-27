package com.project.youtube.dto.auth;

import com.project.youtube.dto.user.CurrentUserResponse;

public record AuthResponse(
        String token,
        String tokenType,
        CurrentUserResponse user
) {
    public static AuthResponse of(String token, CurrentUserResponse user) {
        return new AuthResponse(token, "Bearer", user);
    }
}
