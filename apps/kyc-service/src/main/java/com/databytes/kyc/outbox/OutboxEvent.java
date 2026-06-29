package com.databytes.kyc.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A pending domain event, written in the same transaction as the aggregate it
 * describes. The {@code payload} is stored as {@code jsonb} so it is queryable
 * in the database and emitted verbatim to Kafka by the dispatcher.
 *
 * <p>{@code traceParent} captures the W3C trace context at creation time so the
 * asynchronous dispatch can continue the original request's distributed trace.
 */
@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

    @Id
    public UUID id;

    @Column(name = "aggregate_type", nullable = false)
    public String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    public String aggregateId;

    @Column(nullable = false)
    public String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    public Map<String, Object> payload = new HashMap<>();

    @Column(name = "trace_parent")
    public String traceParent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public OutboxStatus status = OutboxStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "processed_at")
    public Instant processedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
