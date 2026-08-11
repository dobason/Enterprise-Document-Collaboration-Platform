package com.edms.application.ports;

public interface NotificationService {
    void sendNotification(String recipientEmail, String subject, String message);
}
