package com.edms.functions.foldermgmt;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * fn-folder-mgmt (Java version) - tương đương src/functions/folder_mgmt/app.py
 * API POST /folders {folderName, department}
 * Phạm vi đã rút gọn: chỉ 1 cấp thư mục, KHÔNG làm cây lồng nhau.
 */
public class App implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");
    private final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            String rawBody = (String) event.getOrDefault("body", "{}");
            Map<String, Object> body = mapper.readValue(rawBody, Map.class);

            String folderName = (String) body.get("folderName");
            String department = (String) body.get("department");

            if (folderName == null || department == null) {
                return apiResponse(400, Map.of("error", "folderName và department là bắt buộc"));
            }

            String ownerId = extractOwnerId(event);
            String folderId = UUID.randomUUID().toString();

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("PK", AttributeValue.builder().s("DEPT#" + department).build());
            item.put("SK", AttributeValue.builder().s("FOLDER#" + folderId).build());
            item.put("folderId", AttributeValue.builder().s(folderId).build());
            item.put("folderName", AttributeValue.builder().s(folderName).build());
            item.put("department", AttributeValue.builder().s(department).build());
            item.put("ownerId", AttributeValue.builder().s(ownerId).build());
            item.put("createdAt", AttributeValue.builder().n(String.valueOf(Instant.now().getEpochSecond())).build());

            dynamoDb.putItem(PutItemRequest.builder().tableName(TABLE_NAME).item(item).build());

            context.getLogger().log(String.format(
                    "{\"event\":\"folder_created\",\"folderId\":\"%s\",\"department\":\"%s\"}%n",
                    folderId, department));

            // TODO: validate trùng tên thư mục trong cùng department nếu cần
            return apiResponse(201, Map.of("folderId", folderId, "folderName", folderName));

        } catch (Exception e) {
            context.getLogger().log("folder_mgmt_failed: " + e.getMessage());
            return apiResponse(500, Map.of("error", e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private String extractOwnerId(Map<String, Object> event) {
        try {
            Map<String, Object> requestContext = (Map<String, Object>) event.get("requestContext");
            Map<String, Object> authorizer = (Map<String, Object>) requestContext.get("authorizer");
            Map<String, Object> claims = (Map<String, Object>) authorizer.get("claims");
            return (String) claims.getOrDefault("sub", "unknown");
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
