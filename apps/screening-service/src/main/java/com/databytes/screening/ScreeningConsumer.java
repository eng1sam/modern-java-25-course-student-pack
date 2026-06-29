package com.databytes.screening;

import com.databytes.screening.events.ApplicantRegisteredEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.kafka.api.IncomingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import org.apache.kafka.common.header.Header;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

/**
 * Consumes {@code applicant-registered} events and runs a background check on each
 * applicant. The trace and correlation id from the KYC Service flow through
 * automatically (OpenTelemetry) and via the event body, so a single onboarding can
 * be followed across the Kafka boundary.
 *
 * <p>Delivery is at-least-once, so the consumer dedupes on the {@code event-id}
 * header. A failure nacks the message, which the channel's dead-letter strategy
 * routes to {@code applicant-registered-dlq} rather than blocking the partition.
 */
@ApplicationScoped
public class ScreeningConsumer {

    private static final Logger LOG = Logger.getLogger(ScreeningConsumer.class);

    private final ScreeningStore store;
    private final BackgroundCheck backgroundCheck;
    private final ObjectMapper mapper;
    private final MeterRegistry meters;

    @Inject
    public ScreeningConsumer(ScreeningStore store, BackgroundCheck backgroundCheck,
                             ObjectMapper mapper, MeterRegistry meters) {
        this.store = store;
        this.backgroundCheck = backgroundCheck;
        this.mapper = mapper;
        this.meters = meters;
    }

    @Incoming("applicant-registered")
    @Blocking
    public CompletionStage<Void> consume(Message<String> message) {
        try {
            ApplicantRegisteredEvent event =
                    mapper.readValue(message.getPayload(), ApplicantRegisteredEvent.class);
            String eventId = eventId(message);
            MDC.put("correlationId", event.correlationId() != null ? event.correlationId() : "-");
            try {
                if (eventId != null && !store.markProcessed(eventId)) {
                    meters.counter("screenings.duplicate").increment();
                    LOG.infof("Duplicate event %s ignored", eventId);
                } else {
                    Screening screening = backgroundCheck.run(event);
                    store.record(screening);
                    meters.counter("screenings.processed").increment();
                    meters.counter("screenings.decision", "decision", screening.decision().name()).increment();
                    LOG.infof("Screened applicant %d (%s) -> %s (risk %d)",
                            event.applicantId(), event.fullName(),
                            screening.decision(), screening.riskScore());
                }
                return message.ack();
            } finally {
                MDC.remove("correlationId");
            }
        } catch (Exception e) {
            LOG.error("Failed to process applicant-registered event; routing to dead-letter topic", e);
            return message.nack(e);
        }
    }

    private static String eventId(Message<String> message) {
        return message.getMetadata(IncomingKafkaRecordMetadata.class)
                .map(meta -> meta.getHeaders().lastHeader("event-id"))
                .map(ScreeningConsumer::headerValue)
                .orElse(null);
    }

    private static String headerValue(Header header) {
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
