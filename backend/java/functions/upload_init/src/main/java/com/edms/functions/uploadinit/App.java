package com.edms.functions.uploadinit;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * fn-upload-init (Java version)
 * Việc: nhận request {fileName, department, contentType}, sinh S3 pre-signed URL (PUT)
 * để frontend upload file trực tiếp lên S3 - tương đương bản Python src/functions/upload_init/app.py
 *
 * Handler trong template.yaml: com.edms.functions.uploadinit.App::handleRequest
 */
public class App implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final String BUCKET_NAME = System.getenv("BUCKET_NAME");
    private static final int UPLOAD_URL_TTL_SECONDS = 300; // 5 phút

    private final S3Presigner presigner = S3Presigner.create();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            String rawBody = (String) event.getOrDefault("body", "{}");
            Map<String, Object> body = mapper.readValue(rawBody, Map.class);

            String fileName = (String) body.get("fileName");
            String department = (String) body.get("department");
            String contentType = (String) body.getOrDefault("contentType", "application/octet-stream");

            if (fileName == null || department == null) {
                return apiResponse(400, Map.of("error", "fileName và department là bắt buộc"));
            }

            String fileType = fileName.contains(".")
                    ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
                    : "unknown";

            // TODO: validate fileType nằm trong whitelist cho phép (pdf, docx, xlsx, png, jpg...)
            // TODO: validate kích thước file tối đa nếu cần

            String ownerId = extractOwnerId(event);
            String docId = UUID.randomUUID().toString();
            String s3Key = department.toLowerCase() + "/" + docId + "/" + fileName;

            Map<String, String> metadata = new HashMap<>();
            metadata.put("doc-id", docId);
            metadata.put("owner-id", ownerId);
            metadata.put("department", department);
            metadata.put("file-name", fileName);
            metadata.put("file-type", fileType);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(s3Key)
                    .contentType(contentType)
                    .metadata(metadata)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(UPLOAD_URL_TTL_SECONDS))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);

            context.getLogger().log(String.format(
                    "{\"event\":\"presigned_url_generated\",\"docId\":\"%s\",\"department\":\"%s\"}%n",
                    docId, department));

            return apiResponse(200, Map.of(
                    "docId", docId,
                    "uploadUrl", presigned.url().toString(),
                    "s3Key", s3Key,
                    "expiresIn", UPLOAD_URL_TTL_SECONDS
            ));

        } catch (Exception e) {
            context.getLogger().log("upload_init_failed: " + e.getMessage());
            return apiResponse(500, Map.of("error", "Không thể tạo upload URL", "detail", e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private String extractOwnerId(Map<String, Object> event) {
        try {
            Map<String, Object> requestContext = (Map<String, Object>) event.get("requestContext");
            Map<String, Object> authorizer = (Map<String, Object>) requestContext.get("authorizer");
            Map<String, Object> claims = (Map<String, Object>) authorizer.get("claims");
            return (String) claims.getOrDefault("sub", "anonymous");
        } catch (Exception e) {
            return "anonymous";
        }
    }

    private Map<String, Object> apiResponse(int statusCode, Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        response.put("headers", headers);
        try {
            response.put("body", mapper.writeValueAsString(body));
        } catch (Exception e) {
            response.put("body", "{\"error\":\"serialization_failed\"}");
        }
        return response;
    }
}
