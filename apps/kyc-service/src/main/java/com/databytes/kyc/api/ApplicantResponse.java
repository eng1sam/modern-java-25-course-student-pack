package com.databytes.kyc.api;

import com.databytes.kyc.domain.Applicant;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

/** Outbound applicant representation. Keeps the entity out of the HTTP layer. */
public record ApplicantResponse(
        Long id,
        String fullName,
        String nationalId,
        LocalDate dateOfBirth,
        String country,
        String email,
        String status,
        Map<String, String> attributes,
        Instant createdAt) {

    public static ApplicantResponse from(Applicant applicant) {
        return new ApplicantResponse(
                applicant.id, applicant.fullName, applicant.nationalId, applicant.dateOfBirth,
                applicant.country, applicant.email, applicant.status.name(),
                applicant.attributes, applicant.createdAt);
    }
}
