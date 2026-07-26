package com.edms.functions.listdocuments;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test mẫu cho fn-list-documents (khuôn cho các function Java khác trong dự án).
 * Chạy: mvn test (trong thư mục java/functions/list_documents)
 *
 * Lưu ý: đây là test KHÔNG gọi AWS thật (không có DynamoDB Local ở đây) -
 * chỉ verify phần validate input trước khi gọi DynamoDB. Muốn test DynamoDB thật,
 * cân nhắc dùng DynamoDB Local (Docker) hoặc Testcontainers - nằm ngoài scope 4 tuần.
 */
class AppTest {

    @Test
    void missingDepartment_returns400() {
        App app = new App();
        Context context = mockContext();

        Map<String, Object> event = new HashMap<>();
        event.put("queryStringParameters", Map.of("limit", "10")); // thiếu department

        Map<String, Object> result = app.handleRequest(event, context);

        assertEquals(400, result.get("statusCode"));
    }

    private Context mockContext() {
        Context context = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);
        return context;
    }
}
