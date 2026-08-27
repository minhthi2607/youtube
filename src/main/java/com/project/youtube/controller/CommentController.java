package com.project.youtube.controller;

import com.project.youtube.dto.comment.CommentRequest;
import com.project.youtube.dto.comment.CommentResponse;
import com.project.youtube.dto.common.PageResponse;
import com.project.youtube.entity.User;
import com.project.youtube.entity.Video;
import com.project.youtube.security.AuthenticatedUser;
import com.project.youtube.service.CommentService;
import com.project.youtube.service.UserService;
import com.project.youtube.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final VideoService videoService;
    private final UserService userService;

    @PostMapping("/api/videos/{videoId}/comments")
    public ResponseEntity<CommentResponse> create(@PathVariable Long videoId,
                                                    @Valid @RequestBody CommentRequest request,
                                                    @AuthenticationPrincipal AuthenticatedUser principal) {
        Video video = videoService.getVideoOrThrow(videoId);
        User author = userService.getUserOrThrow(principal.getId());
        CommentResponse response = commentService.create(video, author, request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/videos/{videoId}/comments")
    public PageResponse<CommentResponse> list(@PathVariable Long videoId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        Video video = videoService.getVideoOrThrow(videoId);
        return commentService.listForVideo(video, page, size);
    }

    @PutMapping("/api/comments/{id}")
    public CommentResponse update(@PathVariable Long id,
                                    @Valid @RequestBody CommentRequest request,
                                    @AuthenticationPrincipal AuthenticatedUser principal) {
        return commentService.update(id, principal.getId(), request.content());
    }

    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser principal) {
        commentService.delete(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
