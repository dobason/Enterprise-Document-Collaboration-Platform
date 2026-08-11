package com.edms.application.ports;

import com.edms.domain.enums.AuditAction;

public interface AuditService {
    void log(String documentId, AuditAction action, String performedBy, String details);
}
