package com.databytes.kyc.domain;

/** KYC lifecycle states of an applicant. Set by the screening outcome. */
public enum ApplicantStatus {
    /** Registered; background check not yet completed. */
    PENDING,
    /** Background check passed — the applicant can be onboarded. */
    CLEARED,
    /** Flagged for manual review by a compliance officer. */
    REVIEW,
    /** Failed the background check (e.g. watchlist match) — onboarding refused. */
    BLOCKED
}
