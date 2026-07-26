package com.edms.functions.listdocuments;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * fn-list-documents (Java version) - tương đương src/functions/list_documents/app.py
 * API GET /documents?department=SALES&limit=20&nextToken=...
 * Query theo PK=DEPT#<department>, phân trang bằng LastEvaluatedKey (base64 encode/decode).
 */
public class App implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");
    private final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            Map<String, Object> params = (Map<String, Object>) event.getOrDefault("queryStringParameters", Map.of());
            if (params == null) params = Map.of();

            String department = (String) params.get("department");
            int limit = params.containsKey("limit") ? Integer.parseInt((String) params.get("limit")) : 20;
            String nextToken = (String) params.get("nextToken");

            if (department == null) {
                return apiResponse(400, Map.of("error", "department là bắt buộc"));
            }

            Map<String, AttributeValue> exprValues = new HashMap<>();
            exprValues.put(":pk", AttributeValue.builder().s("DEPT#" + department).build());
            exprValues.put(":skPrefix", AttributeValue.builder().s("DOC#").build());

            QueryRequest.Builder queryBuilder = QueryRequest.builder()
                    .tableName(TABLE_NAME)
                    .keyConditionExpression("PK = :pk AND begins_with(SK, :skPrefix)")
                    .expressionAttributeValues(exprValues)
                    .limit(limit);

            if (nextToken != null) {
                Map<String, Object> decoded = mapper.readValue(
                        new String(Base64.getDecoder().decode(nextToken), StandardCharsets.UTF_8), Map.class);
                Map<String, AttributeValue> exclusiveStartKey = new HashMap<>();
                decoded.forEach((k, v) -> exclusiveStartKey.put(k, AttributeValue.builder().s(v.toString()).build()));
                queryBuilder.exclusiveStartKey(exclusiveStartKey);
            }

            QueryResponse result = dynamoDb.query(queryBuilder.build());

            String newNextToken = null;
            if (result.hasLastEvaluatedKey() && !result.lastEvaluatedKey().isEmpty()) {
                Map<String, String> simplified = new HashMap<>();
                result.lastEvaluatedKey().forEach((k, v) -> simplified.put(k, v.s()));
                newNextToken = Base64.getEncoder().encodeToString(
                        mapper.writeValueAsString(simplified).getBytes(StandardCharsets.UTF_8));
            }

            List<Map<String, Object>> items = result.items().stream()
                    .map(this::attributeMapToPlainMap)
                    .toList();

            // TODO: filter theo folderId nếu FE truyền lên (chỉ hiện tài liệu trong 1 thư mục cụ thể)
            Map<String, Object> body = new HashMap<>();
            body.put("items", items);
            body.put("nextToken", newNextToken);
            return apiResponse(200, body);

        } catch (Exception e) {
            context.getLogger().log("list_documents_failed: " + e.getMessage());
            return apiResponse(500, Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> attributeMapToPlainMap(Map<String, AttributeValue> item) {
        Map<String, Object> plain = new HashMap<>();
        item.forEach((k, v) -> {
            if (v.s() != null) plain.put(k, v.s());
            else if (v.n() != null) plain.put(k, v.n());
            else if (v.hasL()) plain.put(k, v.l().stream().map(AttributeValue::s).toList());
        });
        return plain;
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
