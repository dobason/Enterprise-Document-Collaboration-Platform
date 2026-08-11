package com.edms.domain.events;

import java.time.Instant;

public abstract class DomainEvent {
    private final String eventId;
    private final Instant occurredOn;

    protected DomainEvent(String eventId) {
        this.eventId = eventId;
        this.occurredOn = Instant.now();
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }
}
