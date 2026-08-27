package com.project.youtube.controller;

import com.project.youtube.dto.like.LikeStatusResponse;
import com.project.youtube.entity.LikeType;
import com.project.youtube.entity.User;
import com.project.youtube.entity.Video;
import com.project.youtube.security.AuthenticatedUser;
import com.project.youtube.service.LikeService;
import com.project.youtube.service.UserService;
import com.project.youtube.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos/{videoId}")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final VideoService videoService;
    private final UserService userService;

    @PostMapping("/like")
    public LikeStatusResponse like(@PathVariable Long videoId, @AuthenticationPrincipal AuthenticatedUser principal) {
        return react(videoId, principal, LikeType.LIKE);
    }

    @PostMapping("/dislike")
    public LikeStatusResponse dislike(@PathVariable Long videoId, @AuthenticationPrincipal AuthenticatedUser principal) {
        return react(videoId, principal, LikeType.DISLIKE);
    }

    @GetMapping("/likes")
    public LikeStatusResponse status(@PathVariable Long videoId, @AuthenticationPrincipal AuthenticatedUser principal) {
        Video video = videoService.getVideoOrThrow(videoId);
        User user = principal == null ? null : userService.getUserOrThrow(principal.getId());
        return likeService.status(video, user);
    }

    private LikeStatusResponse react(Long videoId, AuthenticatedUser principal, LikeType type) {
        Video video = videoService.getVideoOrThrow(videoId);
        User user = userService.getUserOrThrow(principal.getId());
        return likeService.react(video, user, type);
    }
}
