package com.databytes.screening;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory record of completed screenings. The {@code processedEventIds} set
 * gives at-least-once delivery its idempotency: a redelivered event is
 * recognised and skipped. It is intentionally simple — a production service
 * would dedupe against a durable store (or rely on Kafka exactly-once) so the
 * guarantee survives a restart.
 */
@ApplicationScoped
public class ScreeningStore {

    private static final int MAX_RECENT = 100;

    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();
    private final Deque<Screening> recent = new ConcurrentLinkedDeque<>();

    /** @return {@code true} if this id is new, {@code false} if already processed. */
    public boolean markProcessed(String eventId) {
        return processedEventIds.add(eventId);
    }

    public void record(Screening screening) {
        recent.addFirst(screening);
        while (recent.size() > MAX_RECENT) {
            recent.removeLast();
        }
    }

    public List<Screening> recent() {
        return List.copyOf(recent);
    }

    public long processedCount() {
        return processedEventIds.size();
    }
}
