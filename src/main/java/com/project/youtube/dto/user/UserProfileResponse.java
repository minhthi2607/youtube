package com.project.youtube.dto.user;

import java.time.Instant;

/** Public channel profile — deliberately excludes the email address. */
public record UserProfileResponse(
        Long id,
        String username,
        String displayName,
        String avatarUrl,
        long subscriberCount,
        Instant createdAt
) {
}
