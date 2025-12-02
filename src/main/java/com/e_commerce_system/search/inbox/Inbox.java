package com.e_commerce_system.search.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "inbox",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "inbox_event_id_unique",
                        columnNames = "eventId"
                )
        })
public class Inbox {

    public enum EventStatus {
        PENDING,
        PROCESSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, length = 2048)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public Inbox(Long eventId, String aggregateType, String aggregateId, String eventType, String payload) {
        this.eventId = eventId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = EventStatus.PENDING;
    }

    public void complete() {
        this.status = EventStatus.PROCESSED;
    }
}
