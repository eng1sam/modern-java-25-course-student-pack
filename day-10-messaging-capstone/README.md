# Day 10 — Messaging & capstone

**Goal:** wire the KYC Service to Kafka via Quarkus messaging so it **produces** `applicant-registered`,
and have the Screening Service **consume** it and run a background check — the same topic you drove by
raw CLI on Day 7, now in code. Then run the whole platform and follow one applicant end to end:
REST → PostgreSQL → Kafka → background check.

> Starting point: `git checkout checkpoint/day-09`. The finished platform is `checkpoint/day-10`.
> No source ships in this module — you build on the checkpoint and the two `apps/` services.
> Need the Screening Service from a clean slate? A compiling hello-world skeleton lives in
> `student-pack/skeletons/screening-service/`; add `messaging-kafka` and the `@Incoming` consumer.

> Smoke-test the running platform with the **Postman collection** in `apps/postman/` (or
> `newman run …`): register an applicant, then watch the decision appear in `GET /screenings`.

## Do this

1. **Producer config** (KYC Service `application.properties`): declare
   `mp.messaging.outgoing.applicant-registered` with the `smallrye-kafka` connector,
   `topic=applicant-registered`, a `StringSerializer`. Point `%test` at `smallrye-in-memory` so
   tests need no broker.
2. **Produce** — inject `@Channel("applicant-registered") MutinyEmitter<String>`. In `register`,
   build an `ApplicantRegisteredEvent` from the saved applicant, serialise to JSON, and send it
   **keyed by applicant id** (`OutgoingKafkaRecordMetadata.builder().withKey(...)`) so one
   applicant's events stay on one partition.
3. **Consumer config** (Screening Service): `mp.messaging.incoming.applicant-registered` with
   `smallrye-kafka`, `topic`, a `group.id`, `auto.offset.reset=earliest`, a `StringDeserializer`;
   swap to `smallrye-in-memory` under `%test`.
4. **Consume** — an `@Incoming("applicant-registered") @Blocking` method deserialises to the
   Screening Service's own `ApplicantRegisteredEvent`, runs a `BackgroundCheck` (watchlist + risk
   score → `CLEARED`/`REVIEW`/`BLOCKED`), records a `Screening`, and acks. Use `String.formatted(...)`
   for log messages — not String Templates (absent in JDK 25).
5. **Run it end to end** — build and start Postgres, Kafka, and both services with
   `docker compose -f infra/compose.yaml --profile apps --profile observability up -d --build`,
   then `POST /applicants` (`201`), see the message in Kafka UI (<http://localhost:8081>, topic
   `applicant-registered`), and a result from `GET /screenings`.
6. **Pick one hardening task** — dedupe the consumer on an `event-id` header, route failures to a
   dead-letter topic (`failure-strategy=dead-letter-queue`), or add a container `HEALTHCHECK`.
7. **(Optional demo)** Native image: `./mvnw -B package -Dnative`; compare start-up + memory.

## Done when

- [ ] `./mvnw -B verify` is green in **both** `apps/kyc-service` and `apps/screening-service`
      (in-memory connector + Dev Services Postgres).
- [ ] `docker compose -f infra/compose.yaml --profile apps --profile observability up -d --build`
      brings up Postgres, Kafka, and both services.
- [ ] `POST /applicants` → `201`, and the message appears on `applicant-registered` in Kafka UI.
- [ ] The Screening Service consumes it (via `GET /screenings` or its log) with **no** direct
      call between the two services.
- [ ] One correlation id can be followed across both services' logs.

## Going further

Idempotent consumer (dedupe on `event-id`) · the transactional outbox · `quarkus-oidc` +
`@RolesAllowed` on `POST /applicants` · native-image trade-offs.

Full brief: `labs/day-10/README.md` · Concepts: `docs/content/day-10/` · Reference apps: `apps/kyc-service`, `apps/screening-service`.
