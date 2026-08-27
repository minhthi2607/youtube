package com.project.youtube.dto.video;

import com.project.youtube.dto.user.UserSummaryResponse;

import java.time.Instant;

/** Lighter projection used for video listing/search — avoids per-row like/dislike lookups. */
public record VideoSummaryResponse(
        Long id,
        String title,
        long views,
        UserSummaryResponse uploader,
        Instant createdAt
) {
}
