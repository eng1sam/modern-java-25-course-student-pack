package com.databytes.screening.events;

import java.time.Instant;

/**
 * The Screening Service's own view of the {@code applicant-registered} contract.
 * It deliberately does not share a class with the KYC Service — the Kafka topic
 * is the contract, and each service evolves its copy independently.
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
