package com.databytes.screening;

import java.time.Instant;

/** The result the service produced from a background check on a registered applicant. */
public record Screening(
        Long applicantId,
        String fullName,
        String nationalId,
        ScreeningDecision decision,
        int riskScore,
        String reason,
        Instant screenedAt) {
}
