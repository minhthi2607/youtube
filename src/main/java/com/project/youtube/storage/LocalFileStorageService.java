package com.project.youtube.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path rootLocation;

    public LocalFileStorageService(@Value("${app.storage.location}") String location) {
        this.rootLocation = Paths.get(location).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage directory: " + rootLocation, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Cannot store an empty file");
        }
        String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String storageKey = UUID.randomUUID() + extension;

        Path destination = resolveAndValidate(storageKey);
        try {
            Files.copy(file.getInputStream(), destination);
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + originalFilename, e);
        }
        return storageKey;
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        try {
            Path file = resolveAndValidate(storageKey);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new StorageException("Could not read file: " + storageKey);
        } catch (MalformedURLException e) {
            throw new StorageException("Could not read file: " + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolveAndValidate(storageKey));
        } catch (IOException e) {
            throw new StorageException("Failed to delete file: " + storageKey, e);
        }
    }

    private Path resolveAndValidate(String storageKey) {
        Path resolved = rootLocation.resolve(storageKey).normalize();
        if (!resolved.getParent().equals(rootLocation)) {
            throw new StorageException("Invalid storage key: " + storageKey);
        }
        return resolved;
    }
}
