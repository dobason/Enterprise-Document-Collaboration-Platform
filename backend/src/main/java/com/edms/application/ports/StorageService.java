package com.edms.application.ports;

public interface StorageService {
    String generatePresignedUploadUrl(String fileId, String fileName, String contentType);
    void uploadFile(String key, byte[] content, String contentType);
    byte[] downloadFile(String key);
    void deleteFile(String key);
}
