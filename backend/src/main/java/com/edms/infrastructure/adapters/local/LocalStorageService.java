package com.edms.infrastructure.adapters.local;

import com.edms.api.exception.ResourceNotFoundException;
import com.edms.application.ports.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Profile({"mysql", "aws"})
public class LocalStorageService implements StorageService {

    private final Path uploadDir;

    public LocalStorageService(@Value("${storage.local-dir:./uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir);
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize local storage directory", e);
        }
    }

    @Override
    public String buildKey(String fileId, String fileName) {
        String safeName = (fileName != null && !fileName.isBlank())
                ? Paths.get(fileName).getFileName().toString()
                : fileId;
        return fileId + "_" + safeName;
    }

    @Override
    public String generatePresignedUploadUrl(String fileId, String fileName, String contentType) {
        return "http://localhost:8088/upload/mock-put/" + fileId + "?fileName="
                + URLEncoder.encode(fileName, StandardCharsets.UTF_8);
    }

    @Override
    public void uploadFile(String key, byte[] content, String contentType) {
        try {
            Path path = uploadDir.resolve(key);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file locally", e);
        }
    }

    @Override
    public byte[] downloadFile(String key) {
        try {
            Path path = uploadDir.resolve(key);
            if (!Files.exists(path)) {
                // Mock default file content if file doesn't exist on disk yet
                return ("Mock file content for " + key).getBytes();
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new ResourceNotFoundException("File not found on storage: " + key);
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            Path path = uploadDir.resolve(key);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log warning
        }
    }
}
