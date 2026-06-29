package com.databytes.kyc.events;

import java.time.Instant;

/**
 * The contract published to Kafka when an applicant registers. It is a record so
 * it is immutable and serialises to a flat JSON object. The Screening Service
 * holds its own copy of this shape — the topic is the contract, not a shared class.
 */
public record ApplicantRegisteredEvent(
        Long applicantId,
        String fullName,
        String nationalId,
        String country,
        String email,
        String status,
        Instant occurredAt,
        String correlationId) {
}
