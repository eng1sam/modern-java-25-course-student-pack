package com.databytes.kyc.api;

import com.databytes.kyc.application.ApplicantNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Maps a missing applicant to a 404 with the standard {@link ApiError} body. */
@Provider
public class ApplicantNotFoundExceptionMapper implements ExceptionMapper<ApplicantNotFoundException> {

    @Override
    public Response toResponse(ApplicantNotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(ApiError.of(
                        Response.Status.NOT_FOUND.getStatusCode(),
                        "Not Found",
                        exception.getMessage()))
                .build();
    }
}
