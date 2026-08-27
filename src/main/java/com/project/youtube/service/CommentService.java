package com.project.youtube.service;

import com.project.youtube.dto.comment.CommentResponse;
import com.project.youtube.dto.common.PageResponse;
import com.project.youtube.dto.user.UserSummaryResponse;
import com.project.youtube.entity.Comment;
import com.project.youtube.entity.User;
import com.project.youtube.entity.Video;
import com.project.youtube.exception.ForbiddenException;
import com.project.youtube.exception.ResourceNotFoundException;
import com.project.youtube.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse create(Video video, User author, String content) {
        Comment comment = Comment.builder()
                .content(content)
                .video(video)
                .user(author)
                .build();
        comment = commentRepository.save(comment);
        return toResponse(comment);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> listForVideo(Video video, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository.findByVideoOrderByCreatedAtDesc(video, pageable);
        return PageResponse.from(comments.map(this::toResponse));
    }

    @Transactional
    public CommentResponse update(Long commentId, Long requesterId, String content) {
        Comment comment = getCommentOrThrow(commentId);
        requireOwner(comment, requesterId);
        comment.setContent(content);
        return toResponse(comment);
    }

    @Transactional
    public void delete(Long commentId, Long requesterId) {
        Comment comment = getCommentOrThrow(commentId);
        requireOwner(comment, requesterId);
        commentRepository.delete(comment);
    }

    private void requireOwner(Comment comment, Long requesterId) {
        if (!comment.getUser().getId().equals(requesterId)) {
            throw new ForbiddenException("You can only modify your own comments");
        }
    }

    private Comment getCommentOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + id));
    }

    private CommentResponse toResponse(Comment comment) {
        User user = comment.getUser();
        return new CommentResponse(comment.getId(), comment.getContent(),
                new UserSummaryResponse(user.getId(), user.getUsername(), user.getDisplayName(), user.getAvatarUrl()),
                comment.getCreatedAt(), comment.getUpdatedAt());
    }
}
