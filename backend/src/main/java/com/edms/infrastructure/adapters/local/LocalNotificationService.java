package com.edms.infrastructure.adapters.local;

import com.edms.application.ports.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"mysql", "aws"})
public class LocalNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(LocalNotificationService.class);

    @Override
    public void sendNotification(String recipientEmail, String subject, String message) {
        log.info("LOCAL NOTIFICATION SENT -> To: {}, Subject: {}, Body: {}", recipientEmail, subject, message);
    }
}
