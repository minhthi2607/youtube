package com.project.youtube.repository;

import com.project.youtube.entity.LikeType;
import com.project.youtube.entity.User;
import com.project.youtube.entity.Video;
import com.project.youtube.entity.VideoLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoLikeRepository extends JpaRepository<VideoLike, Long> {

    Optional<VideoLike> findByVideoAndUser(Video video, User user);

    long countByVideoAndType(Video video, LikeType type);

    void deleteByVideoAndUser(Video video, User user);
}
