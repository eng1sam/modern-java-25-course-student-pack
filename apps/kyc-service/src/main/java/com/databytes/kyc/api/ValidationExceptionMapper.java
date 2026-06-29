package com.databytes.kyc.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.time.Instant;
import java.util.List;

/** Turns Bean Validation failures into a 400 with a per-field breakdown. */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<ApiError.FieldViolation> violations = exception.getConstraintViolations().stream()
                .map(v -> new ApiError.FieldViolation(lastNode(v), v.getMessage()))
                .toList();
        ApiError error = new ApiError(
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Bad Request",
                "Request validation failed",
                violations,
                Instant.now());
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }

    private static String lastNode(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int lastDot = path.lastIndexOf('.');
        return lastDot >= 0 ? path.substring(lastDot + 1) : path;
    }
}
