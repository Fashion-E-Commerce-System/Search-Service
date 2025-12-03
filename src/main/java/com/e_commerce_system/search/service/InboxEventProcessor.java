package com.e_commerce_system.search.service;

import com.e_commerce_system.search.inbox.Inbox;
import com.e_commerce_system.search.inbox.InboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InboxEventProcessor {

    private final InboxRepository inboxRepository;
    private final ProductSyncService productSyncService;

    @Scheduled(fixedDelay = 10000) // Process every 10 seconds
    @Transactional
    public void processInboxEvents() {
        log.info("Checking for pending events in inbox...");
        List<Inbox> pendingEvents = inboxRepository.findByStatusOrderByCreatedAtAsc(Inbox.EventStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending events. Processing...", pendingEvents.size());

        for (Inbox event : pendingEvents) {
            try {
                productSyncService.syncProduct(event);
                event.complete();
                inboxRepository.save(event);
            } catch (Exception e) {
                log.error("Error processing event {}: {}", event.getEventId(), e.getMessage(), e);
                // Depending on the error, you might want to implement a retry mechanism
                // or move the event to a dead-letter queue. For now, it will be retried in the next run.
            }
        }
    }
}
