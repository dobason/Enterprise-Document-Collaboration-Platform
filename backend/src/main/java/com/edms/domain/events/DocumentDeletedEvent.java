package com.edms.domain.events;

public class DocumentDeletedEvent extends DomainEvent {
    private final String documentId;
    private final String deletedBy;

    public DocumentDeletedEvent(String eventId, String documentId, String deletedBy) {
        super(eventId);
        this.documentId = documentId;
        this.deletedBy = deletedBy;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getDeletedBy() {
        return deletedBy;
    }
}
