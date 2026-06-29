package com.databytes.kyc.api;

import java.time.Instant;
import java.util.List;

/** Uniform error body returned by the exception mappers. */
public record ApiError(
        int status,
        String error,
        String message,
        List<FieldViolation> violations,
        Instant timestamp) {

    public record FieldViolation(String field, String message) {}

    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, List.of(), Instant.now());
    }
}
