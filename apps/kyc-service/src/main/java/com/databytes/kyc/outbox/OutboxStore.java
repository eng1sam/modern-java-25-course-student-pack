package com.databytes.kyc.outbox;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Transactional boundary for the dispatcher's database work. It is a separate
 * bean from {@code OutboxDispatcher} on purpose: {@code @Transactional} is a CDI
 * interceptor, so each call has to cross a bean boundary to take effect. Keeping
 * these methods here also keeps the Kafka send (slow I/O) outside any open
 * database transaction.
 */
@ApplicationScoped
public class OutboxStore {

    @Inject
    OutboxRepository outbox;

    @Transactional
    public List<OutboxEvent> nextBatch(int size) {
        return outbox.findPending(size);
    }

    @Transactional
    public void markProcessed(UUID id) {
        outbox.findByIdOptional(id).ifPresent(event -> {
            event.status = OutboxStatus.PROCESSED;
            event.processedAt = Instant.now();
        });
    }

    @Transactional
    public void markFailed(UUID id) {
        outbox.findByIdOptional(id).ifPresent(event -> event.status = OutboxStatus.FAILED);
    }
}
