package com.project.youtube.service;

import com.project.youtube.dto.like.LikeStatusResponse;
import com.project.youtube.entity.LikeType;
import com.project.youtube.entity.User;
import com.project.youtube.entity.Video;
import com.project.youtube.entity.VideoLike;
import com.project.youtube.repository.VideoLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final VideoLikeRepository videoLikeRepository;

    @Transactional
    public LikeStatusResponse react(Video video, User user, LikeType type) {
        Optional<VideoLike> existing = videoLikeRepository.findByVideoAndUser(video, user);
        if (existing.isPresent()) {
            VideoLike like = existing.get();
            if (like.getType() == type) {
                videoLikeRepository.delete(like);
            } else {
                like.setType(type);
            }
        } else {
            videoLikeRepository.save(VideoLike.builder().video(video).user(user).type(type).build());
        }
        return status(video, user);
    }

    @Transactional(readOnly = true)
    public LikeStatusResponse status(Video video, User user) {
        long likeCount = videoLikeRepository.countByVideoAndType(video, LikeType.LIKE);
        long dislikeCount = videoLikeRepository.countByVideoAndType(video, LikeType.DISLIKE);
        String currentUserReaction = user == null ? null : videoLikeRepository.findByVideoAndUser(video, user)
                .map(l -> l.getType().name())
                .orElse(null);
        return new LikeStatusResponse(likeCount, dislikeCount, currentUserReaction);
    }
}
