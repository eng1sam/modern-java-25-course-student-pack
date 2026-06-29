package com.databytes.kyc.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.databytes.kyc.domain.Applicant;
import com.databytes.kyc.domain.ApplicantRepository;
import com.databytes.kyc.domain.ApplicantStatus;
import com.databytes.kyc.observability.TraceContextSupport;
import com.databytes.kyc.outbox.OutboxEvent;
import com.databytes.kyc.outbox.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Pure unit test (no Quarkus container): proves the write path persists the
 * applicant and the outbox event together, which is the contract the rest of the
 * system relies on. Repositories are mocked; the real Jackson mapper builds the
 * payload so we exercise serialisation too.
 */
class ApplicantServiceTest {

    private final ApplicantRepository applicants = mock(ApplicantRepository.class);
    private final OutboxRepository outbox = mock(OutboxRepository.class);
    private final TraceContextSupport traces = mock(TraceContextSupport.class);
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    private final ApplicantService service =
            new ApplicantService(applicants, outbox, mapper, traces, meters);

    @Test
    void registerPersistsApplicantAndOutboxEventAtomically() {
        // Simulate the IDENTITY key being assigned on persist.
        doAnswer(invocation -> {
            Applicant persisted = invocation.getArgument(0);
            persisted.id = 42L;
            return null;
        }).when(applicants).persist(any(Applicant.class));

        Applicant draft = new Applicant();
        draft.fullName = "Ada Lovelace";
        draft.nationalId = "LY-1001";
        draft.dateOfBirth = LocalDate.of(1990, 5, 20);
        draft.country = "GB";
        draft.email = "ada@example.com";

        Applicant saved = service.register(draft);

        assertThat(saved.id).isEqualTo(42L);
        assertThat(saved.status).isEqualTo(ApplicantStatus.PENDING);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outbox).persist(captor.capture());
        OutboxEvent event = captor.getValue();

        assertThat(event.type).isEqualTo("applicant-registered");
        assertThat(event.aggregateType).isEqualTo("applicant");
        assertThat(event.aggregateId).isEqualTo("42");
        assertThat(event.payload)
                .containsEntry("fullName", "Ada Lovelace")
                .containsEntry("nationalId", "LY-1001")
                .containsEntry("country", "GB")
                .containsKeys("applicantId", "email", "status");
        assertThat(meters.counter("applicants.registered").count()).isEqualTo(1.0);
    }
}
