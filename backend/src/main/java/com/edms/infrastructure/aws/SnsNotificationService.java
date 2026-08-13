package com.edms.infrastructure.aws;

import com.edms.application.ports.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Service
@Profile("aws")
public class SnsNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(SnsNotificationService.class);

    private final SnsClient snsClient;
    private final String topicArn;

    public SnsNotificationService(SnsClient snsClient,
                                  @Value("${aws.sns.topic-arn}") String topicArn) {
        this.snsClient = snsClient;
        this.topicArn = topicArn;
    }

    @Override
    public void sendNotification(String recipientEmail, String subject, String message) {
        try {
            String fullMessage = "To: " + recipientEmail + "\n\n" + message;
            PublishResponse resp = snsClient.publish(PublishRequest.builder()
                    .topicArn(topicArn)
                    .subject(subject != null ? subject : "EDMS Notification")
                    .message(fullMessage)
                    .build());
            log.info("SNS published messageId={} topic={}", resp.messageId(), topicArn);
        } catch (Exception e) {
            log.error("SNS publish failed for {}: {}", recipientEmail, e.getMessage());
        }
    }
}
