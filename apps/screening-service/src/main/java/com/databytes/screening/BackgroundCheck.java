package com.databytes.screening;

import com.databytes.screening.events.ApplicantRegisteredEvent;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;

/**
 * The (simulated) background check run when an applicant registers.
 *
 * <p><strong>Illustrative only.</strong> A real bank would call sanctions / PEP /
 * adverse-media and credit-reference providers. Here the rules are deterministic so
 * the course can reason about them: a watchlist match is {@code BLOCKED}, a high-risk
 * jurisdiction needs manual {@code REVIEW}, everyone else is {@code CLEARED} with a
 * low risk score derived from the national id.
 */
@ApplicationScoped
public class BackgroundCheck {

    // Obviously fake — placeholders so the demo has a deterministic "hit".
    private static final Set<String> WATCHLIST = Set.of("john doe", "jane roe", "ivan petrov");
    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of("KP", "IR", "SY");

    public Screening run(ApplicantRegisteredEvent applicant) {
        String name = normalise(applicant.fullName());
        if (WATCHLIST.contains(name)) {
            return result(applicant, ScreeningDecision.BLOCKED, 100,
                    "Name matches the sanctions/PEP watchlist");
        }

        int base = baseRisk(applicant.nationalId());
        String country = normalise(applicant.country()).toUpperCase(Locale.ROOT);
        if (HIGH_RISK_COUNTRIES.contains(country)) {
            return result(applicant, ScreeningDecision.REVIEW, Math.max(base, 75),
                    "High-risk jurisdiction (" + country + ") — manual review required");
        }

        return result(applicant, ScreeningDecision.CLEARED, base,
                "Risk score " + base + " — cleared");
    }

    /** Deterministic 0..49 risk derived from the national id (String.hashCode is specified). */
    private static int baseRisk(String nationalId) {
        return nationalId == null ? 0 : Math.floorMod(nationalId.hashCode(), 50);
    }

    private static String normalise(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static Screening result(ApplicantRegisteredEvent a, ScreeningDecision decision,
                                    int risk, String reason) {
        return new Screening(a.applicantId(), a.fullName(), a.nationalId(),
                decision, risk, reason, Instant.now());
    }
}
