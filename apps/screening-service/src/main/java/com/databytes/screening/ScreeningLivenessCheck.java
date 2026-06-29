package com.databytes.screening;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

/**
 * Liveness check that also surfaces how many applicants have been screened.
 * Complements the Kafka connector readiness check the messaging extension adds.
 */
@Liveness
@ApplicationScoped
public class ScreeningLivenessCheck implements HealthCheck {

    @Inject
    ScreeningStore store;

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("screening-processing")
                .up()
                .withData("processed", store.processedCount())
                .build();
    }
}
