# Day 09 — Give the KYC Service a database

**Goal:** replace the in-memory `Map` in `ApplicantService` with real PostgreSQL persistence — an
`applicants` table reached through Java. A registered applicant survives a restart, and the REST
layer from Day 8 doesn't change a line.

> Starting point: `git checkout checkpoint/day-08` (the skeleton with an in-memory store).
> Solution to check against: `git checkout checkpoint/day-09`. No source ships in this module —
> you build on the checkpoint.

## Prerequisites

Docker must be running — **Dev Services** starts a throwaway Postgres for both dev mode and tests.

## Do this

1. **Add extensions** with the CLI (never hand-edit the `pom.xml`):
   ```bash
   quarkus extension add jdbc-postgresql hibernate-orm-panache flyway
   ```
2. **Configure the datasource** in `application.properties` — and leave the URL blank for dev/test
   (a blank URL is what triggers Dev Services):
   ```properties
   quarkus.datasource.db-kind=postgresql
   quarkus.hibernate-orm.database.generation=none
   quarkus.flyway.migrate-at-start=true
   quarkus.hibernate-orm.mapping.format.global=ignore
   ```
3. **V1 migration** at `src/main/resources/db/migration/V1__create_applicants.sql` — a `CREATE TABLE
   applicants` with typed columns (full_name, national_id, date_of_birth, country, email, status),
   a `jsonb attributes` column, and timestamps. The filename *is* the version.
4. **`Applicant` entity** — `@Entity @Table(name="applicants")`, IDENTITY id, `@Enumerated(STRING)`
   status, `@JdbcTypeCode(SqlTypes.JSON)` on the `attributes` `Map`, `@PrePersist`/`@PreUpdate`
   timestamps.
5. **`ApplicantRepository`** — `@ApplicationScoped implements PanacheRepository<Applicant>` with one
   domain query `listNewest(page, size)`; the rest is inherited.
6. **Wire into `ApplicantService`** — inject the repository, delete the `Map` and manual
   id/timestamp code, annotate writes `@Transactional`. **Do not touch `ApplicantResource`** —
   signatures unchanged.
7. **Prove persistence** — `POST /applicants`, stop dev mode, restart, `GET /applicants/{id}` — still there.
8. **Light tests** — a unit test with `mock(ApplicantRepository.class)`, and a `@QuarkusTest` +
   REST Assured test that registers an applicant and asserts `201` / `status "PENDING"`.

## Done when

- [ ] `./mvnw -B verify` is green (unit + `@QuarkusTest` against Dev Services Postgres).
- [ ] A registered applicant survives a restart.
- [ ] Flyway applied `V1`; Hibernate validated the mapping (`generation=none`).
- [ ] `ApplicantResource` is byte-for-byte unchanged from `checkpoint/day-08`.

## Going further

A `V2__…` index migration (e.g. on `national_id`) · the active-record style on a throwaway branch ·
`log.sql=true` to watch the SQL.

Full brief: `labs/day-09/README.md` · Concepts: `docs/content/day-09/` · Reference app: `apps/kyc-service`.
