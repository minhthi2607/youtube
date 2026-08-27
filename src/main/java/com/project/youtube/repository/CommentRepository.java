package com.project.youtube.repository;

import com.project.youtube.entity.Comment;
import com.project.youtube.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByVideoOrderByCreatedAtDesc(Video video, Pageable pageable);

    long countByVideo(Video video);
}
