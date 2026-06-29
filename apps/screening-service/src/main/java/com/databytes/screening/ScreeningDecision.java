package com.databytes.screening;

/** The outcome of a background check. */
public enum ScreeningDecision {
    /** Passed — the applicant can be onboarded. */
    CLEARED,
    /** Needs a human: a compliance officer must review before onboarding. */
    REVIEW,
    /** Failed — e.g. a watchlist match; onboarding is refused. */
    BLOCKED
}
