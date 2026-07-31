package com.edms.domain.events;

public class DocumentUploadedEvent extends DomainEvent {
    private final String documentId;
    private final String ownerId;

    public DocumentUploadedEvent(String eventId, String documentId, String ownerId) {
        super(eventId);
        this.documentId = documentId;
        this.ownerId = ownerId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getOwnerId() {
        return ownerId;
    }
}
