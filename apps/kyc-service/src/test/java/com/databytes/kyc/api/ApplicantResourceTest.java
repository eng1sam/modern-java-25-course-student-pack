package com.databytes.kyc.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

/** Endpoint tests for the applicant API: happy path, missing applicant, validation. */
@QuarkusTest
class ApplicantResourceTest {

    @Test
    void registerThenFetchApplicant() {
        Integer id = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        { "fullName": "Ada Lovelace", "nationalId": "LY-1001",
                          "dateOfBirth": "1990-05-20", "country": "GB", "email": "ada@example.com" }
                        """)
                .when().post("/applicants")
                .then().statusCode(201)
                .header("Location", containsString("/applicants/"))
                .body("id", notNullValue())
                .body("status", is("PENDING"))
                .body("fullName", is("Ada Lovelace"))
                .extract().path("id");

        given().when().get("/applicants/{id}", id)
                .then().statusCode(200)
                .body("nationalId", is("LY-1001"))
                .body("country", is("GB"));
    }

    @Test
    void unknownApplicantReturns404() {
        given().when().get("/applicants/{id}", 999_999)
                .then().statusCode(404)
                .body("error", is("Not Found"));
    }

    @Test
    void invalidApplicantReturns400WithFieldViolations() {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        { "fullName": "", "nationalId": "LY-2", "dateOfBirth": "2999-01-01",
                          "country": "", "email": "not-an-email" }
                        """)
                .when().post("/applicants")
                .then().statusCode(400)
                .body("error", is("Bad Request"))
                .body("violations.field", hasItems("fullName", "country", "dateOfBirth", "email"));
    }
}
