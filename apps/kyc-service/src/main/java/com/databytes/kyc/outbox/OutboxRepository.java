package com.databytes.kyc.outbox;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

/** Panache repository for {@link OutboxEvent} (UUID-keyed). */
@ApplicationScoped
public class OutboxRepository implements PanacheRepositoryBase<OutboxEvent, UUID> {

    /** Oldest-first batch of events still waiting to be relayed to Kafka. */
    public List<OutboxEvent> findPending(int batchSize) {
        return find("status", Sort.by("createdAt").ascending(), OutboxStatus.PENDING)
                .page(Page.ofSize(batchSize))
                .list();
    }

    public long countPending() {
        return count("status", OutboxStatus.PENDING);
    }
}
