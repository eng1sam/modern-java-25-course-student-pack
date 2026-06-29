package com.databytes.screening;

import static org.assertj.core.api.Assertions.assertThat;

import com.databytes.screening.events.ApplicantRegisteredEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BackgroundCheckTest {

    private final BackgroundCheck check = new BackgroundCheck();

    private static ApplicantRegisteredEvent applicant(String name, String nationalId, String country) {
        return new ApplicantRegisteredEvent(1L, name, nationalId, country, "x@example.com",
                "PENDING", Instant.now(), "corr-1");
    }

    @Test
    void watchlistMatchIsBlocked() {
        Screening result = check.run(applicant("John Doe", "LY-1", "GB"));
        assertThat(result.decision()).isEqualTo(ScreeningDecision.BLOCKED);
        assertThat(result.riskScore()).isEqualTo(100);
    }

    @Test
    void ordinaryApplicantInLowRiskCountryIsCleared() {
        Screening result = check.run(applicant("Ada Lovelace", "LY-1001", "GB"));
        assertThat(result.decision()).isEqualTo(ScreeningDecision.CLEARED);
        assertThat(result.riskScore()).isLessThan(50);
    }

    @Test
    void highRiskJurisdictionNeedsReview() {
        Screening result = check.run(applicant("Ada Lovelace", "LY-1001", "KP"));
        assertThat(result.decision()).isEqualTo(ScreeningDecision.REVIEW);
        assertThat(result.riskScore()).isGreaterThanOrEqualTo(70);
    }
}
