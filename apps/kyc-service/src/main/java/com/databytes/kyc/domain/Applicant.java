package com.databytes.kyc.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * The applicant aggregate — a person onboarding to the bank. Fields are public —
 * the idiomatic Quarkus style — and the repository ({@link ApplicantRepository})
 * owns all access so the persistence concern stays testable. A newly registered
 * applicant starts {@code PENDING}; the screening service decides the outcome.
 */
@Entity
@Table(name = "applicants")
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "full_name", nullable = false)
    public String fullName;

    @Column(name = "national_id", nullable = false)
    public String nationalId;

    @Column(name = "date_of_birth", nullable = false)
    public LocalDate dateOfBirth;

    @Column(nullable = false)
    public String country;

    @Column(nullable = false)
    public String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ApplicantStatus status = ApplicantStatus.PENDING;

    /** Free-form, schemaless KYC attributes — stored in a Postgres {@code jsonb} column. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    public Map<String, String> attributes = new HashMap<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    /** Age in whole years on a given date — derived, never stored. */
    public int ageOn(LocalDate asOf) {
        return Period.between(dateOfBirth, asOf).getYears();
    }

    /** A bank can only onboard adults — a simple derived KYC rule. */
    public boolean isAdult() {
        return ageOn(LocalDate.now()) >= 18;
    }
}
