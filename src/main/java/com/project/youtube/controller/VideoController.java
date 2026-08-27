package com.project.youtube.controller;

import com.project.youtube.dto.common.PageResponse;
import com.project.youtube.dto.video.VideoResponse;
import com.project.youtube.dto.video.VideoSummaryResponse;
import com.project.youtube.entity.User;
import com.project.youtube.entity.Video;
import com.project.youtube.security.AuthenticatedUser;
import com.project.youtube.service.UserService;
import com.project.youtube.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private static final long DEFAULT_CHUNK_SIZE = 4 * 1024 * 1024; // 4MB

    private final VideoService videoService;
    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoResponse> upload(@RequestParam String title,
                                                 @RequestParam(required = false) String description,
                                                 @RequestParam("file") MultipartFile file,
                                                 @AuthenticationPrincipal AuthenticatedUser principal) {
        User uploader = userService.getUserOrThrow(principal.getId());
        VideoResponse response = videoService.upload(uploader, title, description, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public PageResponse<VideoSummaryResponse> list(@RequestParam(required = false) String q,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return videoService.search(q, page, size);
    }

    @GetMapping("/{id}")
    public VideoResponse get(@PathVariable Long id) {
        return videoService.getAndRegisterView(id);
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<ResourceRegion> stream(@PathVariable Long id,
                                                   @RequestHeader HttpHeaders headers) throws IOException {
        Video video = videoService.getVideoOrThrow(id);
        Resource resource = videoService.loadVideoResource(video);
        long contentLength = resource.contentLength();

        List<HttpRange> ranges = headers.getRange();
        ResourceRegion region;
        HttpStatus status;
        if (ranges.isEmpty()) {
            long rangeLength = Math.min(DEFAULT_CHUNK_SIZE, contentLength);
            region = new ResourceRegion(resource, 0, rangeLength);
            status = HttpStatus.OK;
        } else {
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(DEFAULT_CHUNK_SIZE, end - start + 1);
            region = new ResourceRegion(resource, start, rangeLength);
            status = HttpStatus.PARTIAL_CONTENT;
        }

        MediaType mediaType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.parseMediaType(video.getContentType()));
        return ResponseEntity.status(status)
                .contentType(mediaType)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(region);
    }
}
