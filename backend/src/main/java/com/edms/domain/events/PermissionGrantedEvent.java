package com.edms.domain.events;

public class PermissionGrantedEvent extends DomainEvent {
    private final String documentId;
    private final String userId;
    private final String role;

    public PermissionGrantedEvent(String eventId, String documentId, String userId, String role) {
        super(eventId);
        this.documentId = documentId;
        this.userId = userId;
        this.role = role;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}
