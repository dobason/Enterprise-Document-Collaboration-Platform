package com.edms.functions.dashboardstats;

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
 * fn-dashboard-stats (Java version) - tương đương src/functions/dashboard_stats/app.py
 * API GET /dashboard/stats
 * Phạm vi STRETCH: trả JSON số lượng tài liệu theo status/department, FE hiển thị bảng đơn giản trước.
 */
public class App implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");
    private static final List<String> STATUSES = List.of("DRAFT", "PENDING", "APPROVED", "REJECTED");

    private final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            // stats: department -> (status -> count)
            Map<String, Map<String, Integer>> stats = new HashMap<>();

            for (String status : STATUSES) {
                Map<String, AttributeValue> exprValues = new HashMap<>();
                exprValues.put(":status", AttributeValue.builder().s(status).build());

                QueryResponse result = dynamoDb.query(QueryRequest.builder()
                        .tableName(TABLE_NAME)
                        .indexName("GSI-ByStatus")
                        .keyConditionExpression("status = :status")
                        .expressionAttributeValues(exprValues)
                        .build());

                for (Map<String, AttributeValue> item : result.items()) {
                    String dept = item.containsKey("department") ? item.get("department").s() : "UNKNOWN";
                    stats.computeIfAbsent(dept, k -> new HashMap<>())
                            .merge(status, 1, Integer::sum);
                }
            }

            context.getLogger().log(String.format(
                    "{\"event\":\"dashboard_stats_computed\",\"departments\":%d}%n", stats.size()));

            // TODO (stretch): cache kết quả 5 phút nếu bảng lớn, tránh query GSI 4 lần mỗi lần mở dashboard
            return apiResponse(200, Map.of("statsByDepartment", stats));

        } catch (Exception e) {
            context.getLogger().log("dashboard_stats_failed: " + e.getMessage());
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
