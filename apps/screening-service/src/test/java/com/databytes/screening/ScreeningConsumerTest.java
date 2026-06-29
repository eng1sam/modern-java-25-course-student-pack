package com.databytes.screening;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * Drives the consumer through the in-memory connector (see %test config) and
 * asserts the event becomes a screening result. No broker required; the real
 * Kafka path is exercised by the Compose end-to-end.
 */
@QuarkusTest
class ScreeningConsumerTest {

    @Inject
    @Any
    InMemoryConnector connector;

    @Inject
    ScreeningStore store;

    @Test
    void anApplicantRegisteredEventBecomesAScreening() throws InterruptedException {
        InMemorySource<String> source = connector.source("applicant-registered");
        int before = store.recent().size();

        source.send("""
                {"applicantId":7,"fullName":"Ada Lovelace","nationalId":"LY-7","country":"GB",
                 "email":"ada@example.com","status":"PENDING",
                 "occurredAt":"2026-01-01T00:00:00Z","correlationId":"corr-7"}
                """);

        long deadline = System.currentTimeMillis() + 10_000;
        while (store.recent().size() <= before && System.currentTimeMillis() < deadline) {
            Thread.sleep(100);
        }

        assertThat(store.recent()).hasSizeGreaterThan(before);
        Screening latest = store.recent().getFirst();
        assertThat(latest.applicantId()).isEqualTo(7L);
        assertThat(latest.decision()).isEqualTo(ScreeningDecision.CLEARED);
    }
}
