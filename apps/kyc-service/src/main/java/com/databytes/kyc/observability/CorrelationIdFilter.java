package com.databytes.kyc.observability;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.util.UUID;
import org.jboss.logging.MDC;

/**
 * Ensures every request carries a correlation id. Reads {@code X-Correlation-Id}
 * (or mints one), puts it in the MDC so it appears on every JSON log line, and
 * echoes it back on the response. The registration path copies the id into the
 * published event, so the Screening Service can log under the same id.
 */
@Provider
public class CorrelationIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String id = requestContext.getHeaderString(HEADER);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, id);
        requestContext.setProperty(MDC_KEY, id);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object id = requestContext.getProperty(MDC_KEY);
        if (id != null) {
            responseContext.getHeaders().putSingle(HEADER, id);
        }
        MDC.remove(MDC_KEY);
    }
}
