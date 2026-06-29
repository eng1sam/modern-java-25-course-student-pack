package com.databytes.kyc.domain;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Panache repository for {@link Applicant}. Using the repository pattern (rather
 * than active record) keeps the entity free of static persistence calls, which
 * makes the service layer straightforward to unit-test with a mock.
 */
@ApplicationScoped
public class ApplicantRepository implements PanacheRepository<Applicant> {

    public List<Applicant> listNewest(int page, int size) {
        return findAll(Sort.by("createdAt").descending())
                .page(Page.of(page, size))
                .list();
    }
}
