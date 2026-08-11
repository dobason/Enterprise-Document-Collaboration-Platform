package com.edms.application.ports;

import com.edms.domain.events.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent event);
}
