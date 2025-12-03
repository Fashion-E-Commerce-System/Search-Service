package com.e_commerce_system.search.consumer;

import com.e_commerce_system.search.event.ProductEvent;
import com.e_commerce_system.search.inbox.Inbox;
import com.e_commerce_system.search.inbox.InboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final InboxRepository inboxRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "product-events", groupId = "search-service-group")
    public void consume(String message) {
        log.info("Kafka event received: {}", message);
        try {
            String eventType;
            Long eventId;
            String aggregateId;
            String payload = message;

            try {
                ProductEvent event = objectMapper.readValue(message, ProductEvent.class);
                eventId = event.getId();
                aggregateId = event.getId().toString();
                if (event.getName() != null && event.getName().startsWith("Updated-")) {
                    eventType = "PRODUCT_UPDATED";
                } else {
                    eventType = "PRODUCT_CREATED";
                }
            } catch (JsonProcessingException e) {
                // If deserialization to ProductEvent fails, try to read it as a Long for deletion.
                try {
                    eventId = objectMapper.readValue(message, Long.class);
                    eventType = "PRODUCT_DELETED";
                    aggregateId = eventId.toString();
                } catch (JsonProcessingException innerEx) {
                    log.error("Could not deserialize event: {}", message, innerEx);
                    return; // Skip malformed message
                }
            }

            if (inboxRepository.existsByEventId(eventId)) {
                log.warn("Duplicate event received, ignoring: {}", message);
                return;
            }

            Inbox inboxItem = Inbox.builder()
                    .eventId(eventId)
                    .aggregateType("Product")
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payload)
                    .build();

            inboxRepository.save(inboxItem);
            log.info("Received and saved event to inbox: {}, type: {}", eventId, eventType);

        } catch (DataIntegrityViolationException e) {
            // This is another way to catch duplicates if existsByEventId check fails due to race conditions
            log.warn("Duplicate event received (caught by DB constraint), ignoring: {}", message);
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }
}
