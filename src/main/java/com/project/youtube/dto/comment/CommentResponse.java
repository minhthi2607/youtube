package com.project.youtube.dto.comment;

import com.project.youtube.dto.user.UserSummaryResponse;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String content,
        UserSummaryResponse author,
        Instant createdAt,
        Instant updatedAt
) {
}
