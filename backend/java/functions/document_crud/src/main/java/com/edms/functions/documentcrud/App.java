package com.edms.functions.documentcrud;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;
import software.amazon.awssdk.services.sfn.model.StartExecutionResponse;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * fn-document-crud (Java version) - tương đương src/functions/document_crud/app.py
 *
 * 2 handler khác nhau (khai báo riêng trong template.yaml):
 *  - handleRequest            -> event S3:ObjectCreated (qua EventBridge) + API GET/DELETE /documents/{docId}
 *  - handleTriggerApproval    -> API POST /documents/{docId}/submit-approval
 */
public class App implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");

    private final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private final S3Client s3 = S3Client.create();
    private final SfnClient sfn = SfnClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        // Case 1: event từ EventBridge khi có file mới trong S3
        if ("aws.s3".equals(event.get("source"))) {
            return handleS3ObjectCreated(event, context);
        }

        // Case 2: gọi qua API Gateway
        String method = (String) event.get("httpMethod");
        Map<String, Object> pathParams = (Map<String, Object>) event.getOrDefault("pathParameters", Map.of());
        String docId = (String) pathParams.get("docId");

        if ("GET".equals(method)) {
            return getDocument(docId);
        } else if ("DELETE".equals(method)) {
            return deleteDocument(docId);
        }
        return apiResponse(400, Map.of("error", "Unsupported method"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleS3ObjectCreated(Map<String, Object> event, Context context) {
        try {
            Map<String, Object> detail = (Map<String, Object>) event.get("detail");
            Map<String, Object> bucketInfo = (Map<String, Object>) detail.get("bucket");
            Map<String, Object> objectInfo = (Map<String, Object>) detail.get("object");
            String bucket = (String) bucketInfo.get("name");
            String key = (String) objectInfo.get("key");

            HeadObjectResponse head = s3.headObject(HeadObjectRequest.builder()
                    .bucket(bucket).key(key).build());
            Map<String, String> meta = head.metadata();

            String docId = meta.getOrDefault("doc-id", "unknown");
            String department = meta.getOrDefault("department", "UNKNOWN");
            String[] keyParts = key.split("/");
            String fallbackFileName = keyParts[keyParts.length - 1];

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("PK", AttributeValue.builder().s("DEPT#" + department).build());
            item.put("SK", AttributeValue.builder().s("DOC#" + docId).build());
            item.put("docId", AttributeValue.builder().s(docId).build());
            item.put("fileName", AttributeValue.builder().s(meta.getOrDefault("file-name", fallbackFileName)).build());
            item.put("fileType", AttributeValue.builder().s(meta.getOrDefault("file-type", "unknown")).build());
            item.put("s3Key", AttributeValue.builder().s(key).build());
            item.put("ownerId", AttributeValue.builder().s(meta.getOrDefault("owner-id", "unknown")).build());
            item.put("department", AttributeValue.builder().s(department).build());
            item.put("status", AttributeValue.builder().s("DRAFT").build());
            item.put("folderId", AttributeValue.builder().s("ROOT").build());
            item.put("createdAt", AttributeValue.builder().n(String.valueOf(Instant.now().getEpochSecond())).build());

            dynamoDb.putItem(PutItemRequest.builder().tableName(TABLE_NAME).item(item).build());

            context.getLogger().log(String.format(
                    "{\"event\":\"document_metadata_saved\",\"docId\":\"%s\",\"department\":\"%s\"}%n",
                    docId, department));

            return apiResponse(200, Map.of("message", "metadata saved", "docId", docId));

        } catch (Exception e) {
            context.getLogger().log("handle_s3_object_created_failed: " + e.getMessage());
            throw new RuntimeException(e); // để Lambda retry / báo lỗi lên CloudWatch Alarm
        }
    }

    private Map<String, Object> getDocument(String docId) {
        // TODO: hiện đang cần biết department để Query theo PK -
        //       cân nhắc thêm GSI-ByDocId nếu muốn get thẳng bằng docId (giống bản Python)
        return apiResponse(501, Map.of("error", "TODO: implement get by docId"));
    }

    private Map<String, Object> deleteDocument(String docId) {
        // TODO: xoá item DynamoDB + xoá object S3 tương ứng, kiểm tra quyền (chỉ owner/admin được xoá)
        return apiResponse(501, Map.of("error", "TODO: implement delete"));
    }

    /**
     * Handler riêng cho API POST /documents/{docId}/submit-approval
     * Khai báo trong template.yaml: Handler: com.edms.functions.documentcrud.App::handleTriggerApproval
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> handleTriggerApproval(Map<String, Object> event, Context context) {
        try {
            Map<String, Object> pathParams = (Map<String, Object>) event.getOrDefault("pathParameters", Map.of());
            String docId = (String) pathParams.get("docId");

            String rawBody = (String) event.getOrDefault("body", "{}");
            Map<String, Object> body = mapper.readValue(rawBody, Map.class);
            String department = (String) body.get("department");

            String stateMachineArn = System.getenv("APPROVAL_STATE_MACHINE_ARN");
            if (stateMachineArn == null) {
                return apiResponse(500, Map.of("error", "APPROVAL_STATE_MACHINE_ARN chưa được cấu hình"));
            }

            Map<String, String> input = Map.of("docId", docId, "department", department);
            StartExecutionResponse execution = sfn.startExecution(StartExecutionRequest.builder()
                    .stateMachineArn(stateMachineArn)
                    .input(mapper.writeValueAsString(input))
                    .build());

            context.getLogger().log(String.format(
                    "{\"event\":\"approval_started\",\"docId\":\"%s\",\"executionArn\":\"%s\"}%n",
                    docId, execution.executionArn()));

            return apiResponse(202, Map.of(
                    "message", "Approval workflow started",
                    "executionArn", execution.executionArn()
            ));

        } catch (Exception e) {
            context.getLogger().log("trigger_approval_failed: " + e.getMessage());
            return apiResponse(500, Map.of("error", e.getMessage()));
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
