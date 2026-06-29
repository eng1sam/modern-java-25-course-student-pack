package com.databytes.screening;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class ScreeningStoreTest {

    @Test
    void marksAnEventProcessedExactlyOnce() {
        ScreeningStore store = new ScreeningStore();

        assertThat(store.markProcessed("evt-1")).isTrue();
        assertThat(store.markProcessed("evt-1")).isFalse();
        assertThat(store.processedCount()).isEqualTo(1);
    }

    @Test
    void keepsMostRecentScreeningFirst() {
        ScreeningStore store = new ScreeningStore();
        store.record(new Screening(1L, "First", "n1", ScreeningDecision.CLEARED, 10, "ok", Instant.now()));
        store.record(new Screening(2L, "Second", "n2", ScreeningDecision.REVIEW, 80, "review", Instant.now()));

        assertThat(store.recent()).first()
                .extracting(Screening::applicantId)
                .isEqualTo(2L);
    }
}
