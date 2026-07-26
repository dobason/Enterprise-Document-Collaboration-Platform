package com.edms.functions.approvaltasks;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * fn-approval-tasks (Java version) - tương đương src/functions/approval_tasks/app.py
 * Được Step Functions gọi trực tiếp (KHÔNG qua API Gateway) với input {action, docId, department}.
 * action: SET_PENDING | SET_APPROVED | SET_REJECTED
 * Lưu ý: input/output ở đây là Map thuần, KHÔNG theo format {statusCode, body} của API Gateway.
 */
public class App implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final String TABLE_NAME = System.getenv("TABLE_NAME");
    private final DynamoDbClient dynamoDb = DynamoDbClient.create();

    private static final Map<String, String> STATUS_MAP = Map.of(
            "SET_PENDING", "PENDING",
            "SET_APPROVED", "APPROVED",
            "SET_REJECTED", "REJECTED"
    );

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        String action = (String) event.get("action");
        String docId = (String) event.get("docId");
        String department = (String) event.get("department");

        String newStatus = STATUS_MAP.get(action);
        if (newStatus == null) {
            throw new IllegalArgumentException("Action không hợp lệ: " + action);
        }

        Map<String, AttributeValue> key = new HashMap<>();
        key.put("PK", AttributeValue.builder().s("DEPT#" + department).build());
        key.put("SK", AttributeValue.builder().s("DOC#" + docId).build());

        Map<String, AttributeValue> exprValues = new HashMap<>();
        exprValues.put(":status", AttributeValue.builder().s(newStatus).build());

        Map<String, String> exprNames = new HashMap<>();
        exprNames.put("#s", "status");

        // TODO: nếu chưa biết SK chính xác, cần Query trước để lấy SK thật (SK=DOC#<docId>)
        dynamoDb.updateItem(UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET #s = :status")
                .expressionAttributeNames(exprNames)
                .expressionAttributeValues(exprValues)
                .build());

        context.getLogger().log(String.format(
                "{\"event\":\"document_status_updated\",\"docId\":\"%s\",\"newStatus\":\"%s\"}%n",
                docId, newStatus));

        Map<String, Object> result = new HashMap<>();
        result.put("docId", docId);
        result.put("status", newStatus);
        return result;
    }
}
