package com.databytes.kyc.application;

/** Thrown when an applicant id does not resolve; mapped to HTTP 404 in the API layer. */
public class ApplicantNotFoundException extends RuntimeException {

    public ApplicantNotFoundException(Long id) {
        super("Applicant " + id + " was not found");
    }
}
