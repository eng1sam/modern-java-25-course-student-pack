package com.databytes.kyc.observability;

import com.databytes.kyc.outbox.OutboxRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Readiness check that fails if the outbox backlog grows unbounded — a sign the
 * dispatcher or Kafka is unhealthy. Complements the datasource and Kafka checks
 * that the extensions register automatically.
 */
@Readiness
@ApplicationScoped
public class OutboxBacklogHealthCheck implements HealthCheck {

    private static final long MAX_BACKLOG = 1000;

    @Inject
    OutboxRepository outbox;

    @Override
    @Transactional
    public HealthCheckResponse call() {
        long pending = outbox.countPending();
        return HealthCheckResponse.named("outbox-backlog")
                .status(pending < MAX_BACKLOG)
                .withData("pending", pending)
                .build();
    }
}
