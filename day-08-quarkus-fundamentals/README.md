# Day 08 — Scaffold the KYC Service

**Goal:** stand up the KYC Service — a Quarkus web service that registers applicants for onboarding,
with validated REST endpoints over an in-memory store, uniform error responses, config profiles,
and health checks, all in dev mode. No database, no Kafka yet; the focus is the framework, DI, and
the HTTP layer.

> Starting point: `git checkout checkpoint/day-07` (a clean repo, no app yet). You **generate** the
> app from scratch — never copy a `pom.xml`. That's why this module has no source of its own.

## Scaffold

```bash
quarkus create app com.databytes:kyc-service \
  --extension=rest-jackson,hibernate-validator,smallrye-health
cd kyc-service
quarkus dev          # or: ./mvnw quarkus:dev
```

Open <http://localhost:8080/q/dev/> and find your beans. Leave dev mode running — edits hot-reload.

## Do this

1. **DTO records** — `RegisterApplicantRequest` (`@NotBlank fullName`, `@NotBlank nationalId`,
   `@NotNull @Past dateOfBirth`, `@NotBlank country`, `@NotBlank @Email email`) and
   `ApplicantResponse` with a `from(...)` factory, so the domain object never leaks over HTTP.
2. **`ApplicantResource`** — `@Inject` an `ApplicantService`; `POST /applicants` takes a `@Valid`
   request and returns `201` with a `Location` header; `GET /applicants/{id}`. Delegate to an
   in-memory store. A new applicant starts `PENDING`.
3. **Error mapping** — `ValidationExceptionMapper` (`ConstraintViolationException` → `400` with
   per-field violations) and a `404` mapper, both returning a uniform `ApiError`.
4. **Exercise with `curl`** — a good applicant (`201`), a bad one (`400` listing fields), a missing
   id (`404`). Edit and re-request; watch live reload.
5. **Health** — `curl localhost:8080/q/health` reports `UP`.

## Done when

- [ ] `./mvnw -B verify` is green.
- [ ] `201` + `Location` on success; `400` with per-field violations; `404` for unknown id — all
      with the uniform `ApiError` body.
- [ ] `/q/health` (and `/live`, `/ready`) respond `UP`.
- [ ] Live reload works with no restart.

## Going further

Paginated `GET /applicants` with `@QueryParam`/`@DefaultValue` · a custom Bean Validation constraint
(e.g. a national-id format check) · explore `/q/dev/`.

> Stuck or behind? `git checkout checkpoint/day-08` is this day's finished state. If `quarkus
> create app` won't cooperate, start from the compiling hello-world skeleton in
> `student-pack/skeletons/kyc-service/` and add the extensions/code from this brief on top.

Full brief: `labs/day-08/README.md` · Concepts: `docs/content/day-08/` · Reference app: `apps/kyc-service`.
