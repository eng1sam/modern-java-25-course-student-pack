package com.databytes.kyc.outbox;

import com.databytes.kyc.observability.TraceContextSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.context.Scope;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduled.ConcurrentExecution;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

/**
 * Relays committed outbox rows to Kafka — the "polling publisher" half of the
 * transactional outbox. One scheduler instance polls and {@code SKIP} prevents
 * overlapping runs; multiple replicas would instead claim rows with
 * {@code SELECT ... FOR UPDATE SKIP LOCKED}. Delivery is at-least-once (a crash
 * between send and mark re-sends), so each message carries a stable
 * {@code event-id} the consumer dedupes on.
 */
@ApplicationScoped
public class OutboxDispatcher {

    private static final Logger LOG = Logger.getLogger(OutboxDispatcher.class);

    private final OutboxStore store;
    private final ObjectMapper mapper;
    private final TraceContextSupport traces;
    private final MeterRegistry meters;
    private final MutinyEmitter<String> emitter;
    private final int batchSize;

    @Inject
    public OutboxDispatcher(OutboxStore store, ObjectMapper mapper, TraceContextSupport traces,
                            MeterRegistry meters,
                            @Channel("applicant-registered") MutinyEmitter<String> emitter,
                            @ConfigProperty(name = "outbox.batch-size", defaultValue = "50") int batchSize) {
        this.store = store;
        this.mapper = mapper;
        this.traces = traces;
        this.meters = meters;
        this.emitter = emitter;
        this.batchSize = batchSize;
    }

    @Scheduled(every = "{outbox.poll-interval}", concurrentExecution = ConcurrentExecution.SKIP)
    void dispatch() {
        for (OutboxEvent event : store.nextBatch(batchSize)) {
            relay(event);
        }
    }

    private void relay(OutboxEvent event) {
        try {
            String value = mapper.writeValueAsString(event.payload);

            RecordHeaders headers = new RecordHeaders();
            headers.add("event-id", event.id.toString().getBytes(StandardCharsets.UTF_8));
            headers.add("event-type", event.type.getBytes(StandardCharsets.UTF_8));

            Message<String> message = Message.of(value).addMetadata(
                    OutgoingKafkaRecordMetadata.<String>builder()
                            .withKey(event.aggregateId)
                            .withHeaders(headers)
                            .build());

            // Continue the trace that registered the applicant, even though we are now on
            // a scheduler thread far from the original HTTP request.
            try (Scope ignored = traces.restore(event.traceParent).makeCurrent()) {
                emitter.sendMessage(message).await().indefinitely();
            }

            store.markProcessed(event.id);
            meters.counter("outbox.dispatched").increment();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to relay outbox event %s", event.id);
            store.markFailed(event.id);
            meters.counter("outbox.failed").increment();
        }
    }
}
