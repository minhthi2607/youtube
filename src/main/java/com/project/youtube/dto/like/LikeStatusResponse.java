package com.project.youtube.dto.like;

public record LikeStatusResponse(
        long likeCount,
        long dislikeCount,
        String currentUserReaction
) {
}
