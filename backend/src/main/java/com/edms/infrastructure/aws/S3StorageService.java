package com.edms.infrastructure.aws;

import com.edms.application.ports.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Primary
@Service
@Profile({"mysql", "aws"})
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucket;
    private final Duration presignDuration;

    public S3StorageService(S3Client s3Client,
                            S3Presigner presigner,
                            @Value("${aws.s3.bucket}") String bucket,
                            @Value("${aws.s3.presign-ttl-seconds:600}") int ttlSeconds) {
        this.s3Client = s3Client;
        this.presigner = presigner;
        this.bucket = bucket;
        this.presignDuration = Duration.ofSeconds(ttlSeconds);
    }

    @Override
    public String generatePresignedUploadUrl(String fileId, String fileName, String contentType) {
        String key = buildKey(fileId, fileName);
        
        // Đảm bảo contentType không bị null/empty
        String validContentType = (contentType != null && !contentType.isBlank()) 
                ? contentType 
                : "application/octet-stream";

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(validContentType)
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .putObjectRequest(putReq)
                .signatureDuration(presignDuration)
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(presignReq);
        return presigned.url().toString();
    }

    @Override
    public void uploadFile(String key, byte[] content, String contentType) {
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(putReq, RequestBody.fromBytes(content));
    }

    @Override
    public byte[] downloadFile(String key) {
        GetObjectRequest getReq = GetObjectRequest.builder().bucket(bucket).key(key).build();
        ResponseBytes<GetObjectResponse> resp = s3Client.getObject(getReq, ResponseTransformer.toBytes());
        return resp.asByteArray();
    }

    @Override
    public void deleteFile(String key) {
        DeleteObjectRequest req = DeleteObjectRequest.builder().bucket(bucket).key(key).build();
        s3Client.deleteObject(req);
    }

    private String buildKey(String fileId, String fileName) {
        String safeName = URLEncoder.encode(fileName == null ? "file" : fileName, StandardCharsets.UTF_8);
        return fileId + "_" + safeName;
    }
}