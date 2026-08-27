package com.project.youtube.dto.user;

import java.time.Instant;

public record CurrentUserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        String avatarUrl,
        Instant createdAt
) {
}
