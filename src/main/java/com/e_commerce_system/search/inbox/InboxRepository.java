package com.e_commerce_system.search.inbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InboxRepository extends JpaRepository<Inbox, Long> {
    List<Inbox> findByStatusOrderByCreatedAt(Inbox.EventStatus status);
}
