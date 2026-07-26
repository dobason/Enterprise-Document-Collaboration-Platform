package com.edms.functions.search;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * fn-search (Java version) - tương đương src/functions/search/app.py
 * API GET /documents/search?fileType=pdf&fileName=hop-dong
 * Dùng GSI-ByType (PK=fileType, SK=fileName) - Query, KHÔNG dùng Scan.
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

            String fileType = (String) params.get("fileType");
            String fileName = (String) params.get("fileName");

            if (fileType == null) {
                return apiResponse(400, Map.of("error", "fileType là bắt buộc để search qua GSI-ByType"));
            }

            Map<String, AttributeValue> exprValues = new HashMap<>();
            exprValues.put(":fileType", AttributeValue.builder().s(fileType).build());
            String keyCondition = "fileType = :fileType";

            if (fileName != null && !fileName.isEmpty()) {
                exprValues.put(":fileNamePrefix", AttributeValue.builder().s(fileName).build());
                keyCondition += " AND begins_with(fileName, :fileNamePrefix)";
            }

            QueryResponse result = dynamoDb.query(QueryRequest.builder()
                    .tableName(TABLE_NAME)
                    .indexName("GSI-ByType")
                    .keyConditionExpression(keyCondition)
                    .expressionAttributeValues(exprValues)
                    .limit(20)
                    .build());

            List<Map<String, Object>> items = result.items().stream()
                    .map(this::attributeMapToPlainMap)
                    .toList();

            context.getLogger().log(String.format(
                    "{\"event\":\"search_executed\",\"fileType\":\"%s\",\"count\":%d}%n",
                    fileType, items.size()));

            // TODO: nếu cần search full-text trong nội dung file, đó là scope OpenSearch (out-of-scope hiện tại)
            return apiResponse(200, Map.of("items", items));

        } catch (Exception e) {
            context.getLogger().log("search_failed: " + e.getMessage());
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
