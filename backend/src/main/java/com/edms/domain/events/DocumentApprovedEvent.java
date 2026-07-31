package com.edms.domain.events;

public class DocumentApprovedEvent extends DomainEvent {
    private final String documentId;
    private final String approvedBy;

    public DocumentApprovedEvent(String eventId, String documentId, String approvedBy) {
        super(eventId);
        this.documentId = documentId;
        this.approvedBy = approvedBy;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getApprovedBy() {
        return approvedBy;
    }
}
