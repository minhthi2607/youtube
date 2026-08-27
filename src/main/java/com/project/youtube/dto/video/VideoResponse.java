package com.project.youtube.dto.video;

import com.project.youtube.dto.user.UserSummaryResponse;

import java.time.Instant;

public record VideoResponse(
        Long id,
        String title,
        String description,
        String contentType,
        long fileSizeBytes,
        long views,
        long likeCount,
        long dislikeCount,
        UserSummaryResponse uploader,
        Instant createdAt
) {
}
