package com.edms.functions.sharelink;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * fn-share-link (Java version) - tương đương src/functions/share_link/app.py
 * API POST /documents/{docId}/share {s3Key, department, sharedWithEmail, ttlMinutes}
 * Gộp chức năng #9 (link chia sẻ có thời hạn) và #6 (chia sẻ cho người dùng khác - báo SNS).
 */
public class App implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");
    private static final String BUCKET_NAME = System.getenv("BUCKET_NAME");
    private static final String SNS_TOPIC_ARN = System.getenv("SNS_TOPIC_ARN");

    private final S3Presigner presigner = S3Presigner.create();
    private final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private final SnsClient sns = SnsClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            Map<String, Object> pathParams = (Map<String, Object>) event.getOrDefault("pathParameters", Map.of());
            String docId = (String) pathParams.get("docId");

            String rawBody = (String) event.getOrDefault("body", "{}");
            Map<String, Object> body = mapper.readValue(rawBody, Map.class);
            String s3Key = (String) body.get("s3Key");
            String department = (String) body.get("department");
            String sharedWithEmail = (String) body.get("sharedWithEmail");
            int ttlMinutes = body.containsKey("ttlMinutes")
                    ? Integer.parseInt(body.get("ttlMinutes").toString()) : 30;

            if (s3Key == null || department == null) {
                return apiResponse(400, Map.of("error", "s3Key và department là bắt buộc"));
            }

            String sharedBy = extractSharedBy(event);

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(ttlMinutes))
                    .getObjectRequest(GetObjectRequest.builder().bucket(BUCKET_NAME).key(s3Key).build())
                    .build();
            PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
            String presignedGetUrl = presigned.url().toString();

            long now = Instant.now().getEpochSecond();
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("PK", AttributeValue.builder().s("DEPT#" + department).build());
            item.put("SK", AttributeValue.builder().s("SHARE#" + docId + "#" + now).build());
            item.put("docId", AttributeValue.builder().s(docId).build());
            item.put("sharedBy", AttributeValue.builder().s(sharedBy).build());
            if (sharedWithEmail != null) {
                item.put("sharedWithEmail", AttributeValue.builder().s(sharedWithEmail).build());
            }
            item.put("expiresAt", AttributeValue.builder().n(String.valueOf(now + ttlMinutes * 60L)).build());

            dynamoDb.putItem(PutItemRequest.builder().tableName(TABLE_NAME).item(item).build());

            if (sharedWithEmail != null && !sharedWithEmail.isEmpty()) {
                sns.publish(PublishRequest.builder()
                        .topicArn(SNS_TOPIC_ARN)
                        .subject("Tài liệu được chia sẻ với bạn - EDMS")
                        .message(String.format(
                                "%s đã chia sẻ 1 tài liệu với bạn.%nLink truy cập (hết hạn sau %d phút): %s",
                                sharedBy, ttlMinutes, presignedGetUrl))
                        .build());
            }

            context.getLogger().log(String.format(
                    "{\"event\":\"document_shared\",\"docId\":\"%s\",\"ttlMinutes\":%d}%n", docId, ttlMinutes));

            // TODO: nếu muốn giới hạn "chỉ email cụ thể mới xem được", cần thêm 1 lớp xác thực nữa
            //       (out-of-scope cho pre-signed URL thuần, vì URL đúng hạn thì ai cầm cũng xem được)
            return apiResponse(200, Map.of("shareUrl", presignedGetUrl, "expiresInMinutes", ttlMinutes));

        } catch (Exception e) {
            context.getLogger().log("share_link_failed: " + e.getMessage());
            return apiResponse(500, Map.of("error", e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private String extractSharedBy(Map<String, Object> event) {
        try {
            Map<String, Object> requestContext = (Map<String, Object>) event.get("requestContext");
            Map<String, Object> authorizer = (Map<String, Object>) requestContext.get("authorizer");
            Map<String, Object> claims = (Map<String, Object>) authorizer.get("claims");
            Object email = claims.get("email");
            return email != null ? (String) email : (String) claims.getOrDefault("sub", "unknown");
        } catch (Exception e) {
            return "unknown";
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
