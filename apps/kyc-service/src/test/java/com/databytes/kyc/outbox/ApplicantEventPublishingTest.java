package com.databytes.kyc.outbox;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;

/**
 * Integration test for the outbox -> Kafka relay. The Kafka connector is swapped
 * for an in-memory one (see %test config), so we can assert the published event
 * deterministically without a broker. Postgres still runs for real.
 */
@QuarkusTest
class ApplicantEventPublishingTest {

    @Inject
    @Any
    InMemoryConnector connector;

    @Test
    void registeringAnApplicantPublishesAnApplicantRegisteredEvent() throws InterruptedException {
        InMemorySink<String> sink = connector.sink("applicant-registered");
        int before = sink.received().size();

        given().contentType(MediaType.APPLICATION_JSON)
                .body("""
                        { "fullName": "Grace Hopper", "nationalId": "LY-9", "dateOfBirth": "1985-12-09",
                          "country": "US", "email": "grace@example.com" }
                        """)
                .when().post("/applicants")
                .then().statusCode(201);

        // The outbox poller relays on a 2s schedule; wait for it to catch up.
        long deadline = System.currentTimeMillis() + 15_000;
        while (sink.received().size() <= before && System.currentTimeMillis() < deadline) {
            Thread.sleep(200);
        }

        assertThat(sink.received()).hasSizeGreaterThan(before);
        Message<String> last = sink.received().get(sink.received().size() - 1);
        assertThat(last.getPayload()).contains("\"nationalId\":\"LY-9\"").contains("\"applicantId\"");
    }
}
