package com.project.youtube.controller;

import com.project.youtube.dto.common.PageResponse;
import com.project.youtube.dto.subscription.SubscriptionStatusResponse;
import com.project.youtube.dto.user.CurrentUserResponse;
import com.project.youtube.dto.user.UserProfileResponse;
import com.project.youtube.dto.video.VideoSummaryResponse;
import com.project.youtube.entity.User;
import com.project.youtube.security.AuthenticatedUser;
import com.project.youtube.service.SubscriptionService;
import com.project.youtube.service.UserService;
import com.project.youtube.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final VideoService videoService;
    private final SubscriptionService subscriptionService;

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return userService.getCurrentUser(principal.getId());
    }

    @GetMapping("/{id}")
    public UserProfileResponse profile(@PathVariable Long id) {
        return userService.getProfile(id);
    }

    @GetMapping("/{id}/videos")
    public PageResponse<VideoSummaryResponse> videos(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        User uploader = userService.getUserOrThrow(id);
        return videoService.findByUploader(uploader, page, size);
    }

    @PostMapping("/{id}/subscribe")
    public SubscriptionStatusResponse subscribe(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser principal) {
        User subscriber = userService.getUserOrThrow(principal.getId());
        User channel = userService.getUserOrThrow(id);
        return subscriptionService.subscribe(subscriber, channel);
    }

    @DeleteMapping("/{id}/subscribe")
    public SubscriptionStatusResponse unsubscribe(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser principal) {
        User subscriber = userService.getUserOrThrow(principal.getId());
        User channel = userService.getUserOrThrow(id);
        return subscriptionService.unsubscribe(subscriber, channel);
    }

    @GetMapping("/{id}/subscribe")
    public SubscriptionStatusResponse subscriptionStatus(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser principal) {
        User subscriber = userService.getUserOrThrow(principal.getId());
        User channel = userService.getUserOrThrow(id);
        return subscriptionService.status(subscriber, channel);
    }
}
