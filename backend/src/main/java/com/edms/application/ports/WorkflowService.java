package com.edms.application.ports;

public interface WorkflowService {
    void submitForApproval(String documentId, String submittedBy);
    void approveDocument(String documentId, String approvedBy);
    void rejectDocument(String documentId, String rejectedBy, String reason);
}
