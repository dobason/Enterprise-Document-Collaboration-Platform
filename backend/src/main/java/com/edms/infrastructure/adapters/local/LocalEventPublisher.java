package com.edms.infrastructure.adapters.local;

import com.edms.application.ports.EventPublisher;
import com.edms.domain.events.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"mysql", "aws"})
public class LocalEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LocalEventPublisher.class);
    private final ApplicationEventPublisher springPublisher;

    public LocalEventPublisher(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        log.info("LOCAL DOMAIN EVENT PUBLISHED: type={}, id={}, occurredOn={}",
                event.getClass().getSimpleName(), event.getEventId(), event.getOccurredOn());
        springPublisher.publishEvent(event);
    }
}
