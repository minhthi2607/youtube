package com.project.youtube.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Storage abstraction for uploaded video files.
 * The dev implementation ({@link LocalFileStorageService}) writes to the local
 * filesystem; swapping to a cloud backend (S3, GCS, ...) later only requires a
 * new implementation of this interface, callers never touch the filesystem directly.
 */
public interface FileStorageService {

    /**
     * Persists the given multipart file and returns an opaque storage key that
     * can later be passed to {@link #loadAsResource(String)} or {@link #delete(String)}.
     */
    String store(MultipartFile file);

    Resource loadAsResource(String storageKey);

    void delete(String storageKey);
}
