package com.e_commerce_system.search.inbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InboxRepository extends JpaRepository<Inbox, UUID> {
    boolean existsByEventId(Long eventId);

    List<Inbox> findByStatusOrderByCreatedAtAsc(Inbox.EventStatus status);
}