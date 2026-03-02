package com.hypermall.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    /**
     * Publish event locally using Spring's ApplicationEventPublisher
     */
    public void publishLocal(BaseEvent event) {
        log.info("Publishing local event: {} with id: {}", event.getEventType(), event.getEventId());
        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Publish event to RabbitMQ for cross-service communication
     */
    public void publishToQueue(String exchange, String routingKey, BaseEvent event) {
        if (rabbitTemplate == null) {
            log.warn("RabbitTemplate not configured. Falling back to local event publishing.");
            publishLocal(event);
            return;
        }

        log.info("Publishing event to queue: exchange={}, routingKey={}, eventType={}, eventId={}",
                exchange, routingKey, event.getEventType(), event.getEventId());
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    /**
     * Publish event to a specific queue
     */
    public void publishToQueue(String queueName, BaseEvent event) {
        if (rabbitTemplate == null) {
            log.warn("RabbitTemplate not configured. Falling back to local event publishing.");
            publishLocal(event);
            return;
        }

        log.info("Publishing event to queue: {}, eventType={}, eventId={}",
                queueName, event.getEventType(), event.getEventId());
        rabbitTemplate.convertAndSend(queueName, event);
    }
}
