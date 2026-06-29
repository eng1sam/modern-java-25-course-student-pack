# apps/ — reference implementation conventions

This directory is the **final, finished** order-processing platform. It is the spine of the
course; treat it as production-quality reference code that experienced developers will read
line by line.

## Services

- `kyc-service/` — REST API for applicant onboarding (KYC). Owns the `applicants` schema in
  PostgreSQL, validates input, persists via Panache, and publishes `applicant-registered`
  events to Kafka.
- `screening-service/` — Consumes `applicant-registered` events and runs a background check
  (watchlist + risk score) on each applicant.

## Mandatory Quarkus idioms (current for 3.33 LTS)

- Scaffold with the `quarkus` CLI. REST extension is **quarkus-rest** ("Quarkus REST"),
  with `quarkus-rest-jackson` for JSON. Never write the legacy "RESTEasy Reactive" name.
- Persistence: Hibernate ORM with **Panache** (repository pattern preferred for testability),
  **Flyway** for versioned migrations, **Dev Services** for zero-config Postgres in dev/test.
- Messaging: **SmallRye Reactive Messaging** with `@Incoming`/`@Outgoing` channels.
- Config: `application.properties` with `%dev`/`%test`/`%prod` profiles; inject with
  `@ConfigProperty`.
- Validation: Bean Validation annotations + an `ExceptionMapper` for clean error responses.
- Observability: SmallRye Health (liveness/readiness/startup), Micrometer metrics, and
  OpenTelemetry tracing with context propagation across the Kafka boundary.
- Reliability (Day 9): transactional **outbox** pattern, idempotency keys, dead-letter topics.

## Modern Java 25 to use (and what to avoid)

- Use: records as DTOs/value objects, sealed interfaces for domain states, pattern matching
  in switch, virtual threads for blocking calls, structured concurrency where it fits.
- Avoid: **String Templates** (withdrawn in JDK 23, absent in 25). Use text blocks +
  `String.formatted(...)`.

## Testing — the gate for every checkpoint

- Unit tests for business logic, `@QuarkusTest` for CDI-wired tests, REST Assured for
  endpoints, Testcontainers (via Dev Services) for real Postgres and Kafka.
- `./mvnw -B verify` must be green before tagging any `checkpoint/day-NN`.
