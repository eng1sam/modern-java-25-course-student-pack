package com.databytes.kyc.application;

import com.databytes.kyc.domain.Applicant;
import com.databytes.kyc.domain.ApplicantRepository;
import com.databytes.kyc.domain.ApplicantStatus;
import com.databytes.kyc.events.ApplicantRegisteredEvent;
import com.databytes.kyc.observability.TraceContextSupport;
import com.databytes.kyc.outbox.OutboxEvent;
import com.databytes.kyc.outbox.OutboxRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.jboss.logging.MDC;

/**
 * Application service for applicants. The write path is the heart of the
 * reference app: persisting the applicant and writing its event to the outbox
 * happen in one transaction, so they either both commit or both roll back.
 * Nothing is sent to Kafka here — the {@code OutboxDispatcher} relays committed
 * events afterwards, and the screening service runs the background check.
 */
@ApplicationScoped
public class ApplicantService {

    private static final TypeReference<Map<String, Object>> JSON_OBJECT = new TypeReference<>() {};

    private final ApplicantRepository applicants;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final TraceContextSupport traces;
    private final MeterRegistry meters;

    @Inject
    public ApplicantService(ApplicantRepository applicants, OutboxRepository outbox, ObjectMapper mapper,
                            TraceContextSupport traces, MeterRegistry meters) {
        this.applicants = applicants;
        this.outbox = outbox;
        this.mapper = mapper;
        this.traces = traces;
        this.meters = meters;
    }

    @Transactional
    public Applicant register(Applicant draft) {
        draft.status = ApplicantStatus.PENDING;
        applicants.persist(draft); // IDENTITY key: inserted immediately, draft.id is now set

        ApplicantRegisteredEvent event = new ApplicantRegisteredEvent(
                draft.id, draft.fullName, draft.nationalId, draft.country,
                draft.email, draft.status.name(), Instant.now(), currentCorrelationId());

        OutboxEvent record = new OutboxEvent();
        record.aggregateType = "applicant";
        record.aggregateId = String.valueOf(draft.id);
        record.type = "applicant-registered";
        record.payload = mapper.convertValue(event, JSON_OBJECT);
        record.traceParent = traces.captureTraceParent();
        outbox.persist(record);

        meters.counter("applicants.registered").increment();
        return draft;
    }

    @Transactional
    public Applicant findById(Long id) {
        Applicant applicant = applicants.findById(id);
        if (applicant == null) {
            throw new ApplicantNotFoundException(id);
        }
        return applicant;
    }

    @Transactional
    public List<Applicant> list(int page, int size) {
        return applicants.listNewest(page, size);
    }

    private static String currentCorrelationId() {
        Object id = MDC.get("correlationId");
        return id == null ? null : id.toString();
    }
}
