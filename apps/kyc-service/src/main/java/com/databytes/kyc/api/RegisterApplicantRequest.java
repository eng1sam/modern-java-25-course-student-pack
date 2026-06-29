package com.databytes.kyc.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Map;

/**
 * Inbound applicant registration. Bean Validation annotations on the record
 * components are enforced by the {@code @Valid} parameter on the resource;
 * failures are turned into a clean 400 by {@code ValidationExceptionMapper}.
 */
public record RegisterApplicantRequest(
        @NotBlank String fullName,
        @NotBlank String nationalId,
        @NotNull @Past LocalDate dateOfBirth,
        @NotBlank String country,
        @NotBlank @Email String email,
        Map<String, String> attributes) {
}
