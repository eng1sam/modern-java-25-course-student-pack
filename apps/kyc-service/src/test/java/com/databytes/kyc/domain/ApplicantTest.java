package com.databytes.kyc.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ApplicantTest {

    @Test
    void ageOnIsWholeYearsBetweenBirthAndDate() {
        Applicant applicant = new Applicant();
        applicant.dateOfBirth = LocalDate.of(2000, 1, 1);

        assertThat(applicant.ageOn(LocalDate.of(2020, 1, 1))).isEqualTo(20);
        assertThat(applicant.ageOn(LocalDate.of(2019, 12, 31))).isEqualTo(19);
    }

    @Test
    void isAdultReflectsTheEighteenYearRule() {
        Applicant adult = new Applicant();
        adult.dateOfBirth = LocalDate.now().minusYears(30);
        assertThat(adult.isAdult()).isTrue();

        Applicant minor = new Applicant();
        minor.dateOfBirth = LocalDate.now().minusYears(10);
        assertThat(minor.isAdult()).isFalse();
    }
}
