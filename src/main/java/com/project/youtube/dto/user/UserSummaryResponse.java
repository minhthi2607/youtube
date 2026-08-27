package com.project.youtube.dto.user;

public record UserSummaryResponse(
        Long id,
        String username,
        String displayName,
        String avatarUrl
) {
}
