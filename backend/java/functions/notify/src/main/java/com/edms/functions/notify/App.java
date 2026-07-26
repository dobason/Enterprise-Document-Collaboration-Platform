package com.edms.functions.notify;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * fn-notify (Java version) - tương đương src/functions/notify/app.py
 * Được Step Functions gọi (waitForTaskToken pattern) khi cần approver duyệt tài liệu.
 * Publish message qua SNS kèm taskToken (approver dùng lệnh CLI để giả lập duyệt trong giai đoạn lab -
 * xem hướng dẫn trong EDMS-Serverless-Roadmap.md mục 5.6).
 */
public class App implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final String SNS_TOPIC_ARN = System.getenv("SNS_TOPIC_ARN");
    private final SnsClient sns = SnsClient.create();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        String docId = (String) event.get("docId");
        String taskToken = (String) event.get("taskToken");

        String message = String.format(
                "Tài liệu %s đang chờ phê duyệt.%n%n" +
                        "Để duyệt (giả lập trong giai đoạn lab), chạy lệnh:%n" +
                        "aws stepfunctions send-task-success --task-token '%s' --task-output '{\"decision\":\"APPROVED\"}'%n%n" +
                        "Để từ chối:%n" +
                        "aws stepfunctions send-task-success --task-token '%s' --task-output '{\"decision\":\"REJECTED\"}'",
                docId, taskToken, taskToken);

        sns.publish(PublishRequest.builder()
                .topicArn(SNS_TOPIC_ARN)
                .subject("Tài liệu cần bạn phê duyệt - EDMS")
                .message(message)
                .build());

        context.getLogger().log(String.format(
                "{\"event\":\"approval_notification_sent\",\"docId\":\"%s\"}%n", docId));

        // TODO (stretch, nếu có thời gian ở Weekend 4): thay lệnh CLI trên bằng 1 API endpoint
        //      cho approver bấm nút Approve/Reject trên UI thay vì chạy CLI thủ công
        Map<String, Object> result = new HashMap<>();
        result.put("status", "notified");
        return result;
    }
}
