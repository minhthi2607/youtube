package com.project.youtube.dto.subscription;

public record SubscriptionStatusResponse(
        boolean subscribed,
        long subscriberCount
) {
}
