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
            ProductEvent event;
            try {
                // First, try to deserialize as a full ProductEvent object
                event = objectMapper.readValue(message, ProductEvent.class);
            } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
                // If it fails because it's a number, try to deserialize as Long
                if (e.getMessage() != null && e.getMessage().contains("deserialize from Number value")) {
                    try {
                        Long eventId = objectMapper.readValue(message, Long.class);
                        // Create a ProductEvent with just the ID
                        event = new ProductEvent(eventId, null); // Assuming null name is acceptable
                        log.warn("Received product event as a raw ID: {}. Processing with null name.", eventId);
                    } catch (JsonProcessingException innerEx) {
                        // If it's not even a Long, re-throw the original MismatchedInputException
                        throw e; // Re-throw the original MismatchedInputException
                    }
                } else {
                    // Re-throw other MismatchedInputExceptions
                    throw e;
                }
            }

            Inbox inboxItem = Inbox.builder()
                    .eventId(event.getId().longValue())
                    .aggregateType("Product")
                    .aggregateId(event.getId().toString())
                    .eventType("ProductUpdated") // Assuming this is an update event or a generic product event
                    .payload(message)
                    .build();

            inboxRepository.save(inboxItem);
            log.info("Received and saved event to inbox: {}", event.getId());

        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate event received, ignoring: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Could not deserialize event: {}", message, e);
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }
}
