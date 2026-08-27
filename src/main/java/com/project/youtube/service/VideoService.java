package com.project.youtube.service;

import com.project.youtube.dto.common.PageResponse;
import com.project.youtube.dto.user.UserSummaryResponse;
import com.project.youtube.dto.video.VideoResponse;
import com.project.youtube.dto.video.VideoSummaryResponse;
import com.project.youtube.entity.LikeType;
import com.project.youtube.entity.User;
import com.project.youtube.entity.Video;
import com.project.youtube.exception.BadRequestException;
import com.project.youtube.exception.ResourceNotFoundException;
import com.project.youtube.repository.VideoLikeRepository;
import com.project.youtube.repository.VideoRepository;
import com.project.youtube.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class VideoService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("video/mp4", "video/webm");

    private final VideoRepository videoRepository;
    private final VideoLikeRepository videoLikeRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public VideoResponse upload(User uploader, String title, String description, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("A video file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Unsupported video format. Only mp4 and webm are accepted");
        }

        String storageKey = fileStorageService.store(file);

        Video video = Video.builder()
                .title(title)
                .description(description)
                .filePath(storageKey)
                .contentType(contentType)
                .fileSizeBytes(file.getSize())
                .uploader(uploader)
                .build();
        video = videoRepository.save(video);

        return toVideoResponse(video, 0, 0);
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoSummaryResponse> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Video> videos = StringUtils.hasText(query)
                ? videoRepository.findByTitleContainingIgnoreCase(query, pageable)
                : videoRepository.findAll(pageable);
        return PageResponse.from(videos.map(this::toVideoSummaryResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoSummaryResponse> findByUploader(User uploader, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Video> videos = videoRepository.findByUploader(uploader, pageable);
        return PageResponse.from(videos.map(this::toVideoSummaryResponse));
    }

    @Transactional
    public VideoResponse getAndRegisterView(Long id) {
        Video video = getVideoOrThrow(id);
        videoRepository.incrementViews(id);
        long likeCount = videoLikeRepository.countByVideoAndType(video, LikeType.LIKE);
        long dislikeCount = videoLikeRepository.countByVideoAndType(video, LikeType.DISLIKE);
        return toVideoResponse(video, likeCount, dislikeCount, video.getViews() + 1);
    }

    public Video getVideoOrThrow(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found: " + id));
    }

    public Resource loadVideoResource(Video video) {
        return fileStorageService.loadAsResource(video.getFilePath());
    }

    private VideoResponse toVideoResponse(Video video, long likeCount, long dislikeCount) {
        return toVideoResponse(video, likeCount, dislikeCount, video.getViews());
    }

    private VideoResponse toVideoResponse(Video video, long likeCount, long dislikeCount, long views) {
        return new VideoResponse(video.getId(), video.getTitle(), video.getDescription(), video.getContentType(),
                video.getFileSizeBytes(), views, likeCount, dislikeCount, toUserSummary(video.getUploader()),
                video.getCreatedAt());
    }

    private VideoSummaryResponse toVideoSummaryResponse(Video video) {
        return new VideoSummaryResponse(video.getId(), video.getTitle(), video.getViews(),
                toUserSummary(video.getUploader()), video.getCreatedAt());
    }

    private UserSummaryResponse toUserSummary(User user) {
        return new UserSummaryResponse(user.getId(), user.getUsername(), user.getDisplayName(), user.getAvatarUrl());
    }
}
