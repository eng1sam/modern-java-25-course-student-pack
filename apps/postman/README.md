# KYC Platform — Postman collections & Day-1 demo

Everything you need to **run, test, and demo** the reference platform in front of students on
day one: two Postman collections (one per service), a shared environment, and a step-by-step
demo script.

> **The story you'll tell:** a customer registers for onboarding (KYC). That single REST call is
> saved to PostgreSQL and, in the *same transaction*, an event is written to an **outbox**. The
> event is relayed to **Kafka**. A separate **Screening Service** consumes it, runs a background
> check (watchlist + risk score), and decides **CLEARED**, **REVIEW**, or **BLOCKED** — with no
> direct call between the two services. One request, two services, an event bus in the middle.

```
POST /applicants ─▶ KYC Service ─▶ PostgreSQL (applicant + outbox, one tx)
                                      │
                                      ▼  outbox dispatcher
                               Kafka: applicant-registered
                                      │
                                      ▼
                          Screening Service ─▶ background check ─▶ GET /screenings
```

## Files

| File | What it is |
|------|------------|
| `KYC-Service.postman_collection.json` | All KYC endpoints + the 3 demo applicants (CLEARED/REVIEW/BLOCKED) |
| `Screening-Service.postman_collection.json` | All Screening endpoints (`GET /screenings`, health, metrics) |
| `DataBytes-Local.postman_environment.json` | `kycBaseUrl`, `screeningBaseUrl`, and runtime vars |

---

## 1. Prerequisites

- **Docker** (Compose V2) — `docker compose version`
- **JDK 25** + the Maven wrapper (`./mvnw`) — *not* needed for the Docker demo; only for `task test` or dev-mode iteration
- **Postman** (desktop) *or* **Newman** (`npm i -g newman`) for the CLI path
- From the repo root. `mise install` provisions the toolchain if you use [mise](https://mise.jdx.dev).

---

## 2. Run both services

One command brings up Postgres, Kafka, Kafka UI, **and both services** sharing the same broker:

```bash
docker compose -f infra/compose.yaml --profile apps up -d --build
```

The first run builds the two service images (downloads dependencies) — give it a few minutes.
Subsequent starts are seconds. (`task up:all` is a shortcut that also starts the observability
stack.)

| Service | URL |
|---------|-----|
| KYC Service | http://localhost:8080 |
| Screening Service | http://localhost:8082 |
| Kafka UI | http://localhost:8081 |
| PostgreSQL | localhost:5432 (`orders`/`orders`) |

Wait until both report ready, then you're set:

```bash
curl -s localhost:8080/q/health/ready | jq .status   # "UP"
curl -s localhost:8082/q/health/ready | jq .status   # "UP"
```

> **Want the dashboards too?** Add `--profile observability` to also start OTel Collector,
> Prometheus (9090) and Grafana (3000) for a metrics flourish at the end.

> **Editing code later?** For live-reload development you can run a single service with
> `./mvnw quarkus:dev` (Dev Services give it a throwaway Postgres/Kafka). That's for iterating on
> one service — the **cross-service Kafka demo needs the shared broker above**, so use the Docker
> stack for the demo itself.

---

## 3. Load the collections

**Postman:** *Import* → drop in all three files → select **Data Bytes — Local** as the active
environment (top-right) so `{{kycBaseUrl}}` / `{{screeningBaseUrl}}` resolve.

**Newman (CLI):**

```bash
cd apps/postman
newman run KYC-Service.postman_collection.json       -e DataBytes-Local.postman_environment.json
sleep 3   # let the events flow through Kafka and get screened
newman run Screening-Service.postman_collection.json -e DataBytes-Local.postman_environment.json
```

---

## 4. The Day-1 demo script (≈5 minutes)

Open three things side by side: **Postman**, **Kafka UI** (http://localhost:8081), and a terminal
tailing the logs (`docker compose -f infra/compose.yaml logs -f kyc-service screening-service`).

1. **Show it's empty.** In the *Screening Service* collection run **Recent screenings** → `[]`.
   "Nothing has happened yet."
2. **Register a clean applicant.** In *KYC Service* run **Register applicant — CLEARED**. Point out:
   - `201 Created` + a `Location` header → the new applicant, status `PENDING`.
   - The response echoes back the **`X-Correlation-Id`** we sent (the test asserts it). That id is
     our thread through the whole system.
3. **Watch the event land.** Switch to **Kafka UI → Topics → `applicant-registered` → Messages** —
   the event is there. "The KYC Service never called the Screening Service. It just published a fact."
4. **Register the other two.** Run **Register — REVIEW** (high-risk country `IR`) and
   **Register — BLOCKED** (watchlist name `John Doe`).
5. **See the decisions.** Back in *Screening Service* run **Recent screenings** again → three results:
   `CLEARED`, `REVIEW` (risk ≥ 75), `BLOCKED` (risk 100, watchlist reason). Open the Postman Console
   (*View → Show Postman Console*) to show the decision tally the test prints.
6. **Follow one onboarding across the bus.** The id we sent in step 2 came back in the KYC
   response header *and* is copied into the Kafka event. Grep the **Screening Service** logs for it
   and you'll see that same id on the line where it screened applicant #1 (JSON logs, `mdc.correlationId`):

   ```bash
   docker compose -f infra/compose.yaml logs screening-service | grep "$CID"   # CID from step 2
   ```

   "One id, minted at the front door, traced across a network and a message bus into another service."
   (The KYC service carries the id in its MDC and response header; it just doesn't log a business line
   on the register path, so grep the *screening* logs to see it.)
7. *(Optional)* Show **metrics**: run *KYC* **Prometheus metrics** (`applicants_registered_total`)
   and *Screening* **Prometheus metrics** (`screenings_decision_total{decision="BLOCKED"}`).

**Why the outcomes differ** (deterministic, so the demo always works — see
`apps/screening-service/.../BackgroundCheck.java`):

| Applicant | Rule that fires | Decision |
|-----------|-----------------|----------|
| Amina Yusuf (SD) | none → low risk score | **CLEARED** |
| Reza Karimi (IR) | country in high-risk list | **REVIEW** |
| John Doe (US) | name on the watchlist | **BLOCKED** |

---

## 5. Test it (assertions, not just eyeballing)

Each request carries `pm.test(...)` assertions (status codes, `Location` header, `PENDING` status,
echoed correlation id, the `400` field violations, the `404` shape). Run them all:

- **Postman:** *Collection → Run* on each collection (KYC first, then Screening).
- **Newman:** the commands in §3 — green means every assertion passed.

To prove the *code* itself (not just a running instance), run the service test suites — real
Postgres/Kafka via Testcontainers:

```bash
task test        # ./mvnw -B verify in both apps/kyc-service and apps/screening-service
```

---

## 6. Variables

| Variable | Default | Set by |
|----------|---------|--------|
| `kycBaseUrl` | `http://localhost:8080` | environment |
| `screeningBaseUrl` | `http://localhost:8082` | environment |
| `applicantId` | _(runtime)_ | "Register applicant — CLEARED" test script |
| `correlationId` | _(runtime)_ | "Register applicant — CLEARED" pre-request script |

---

## 7. Troubleshooting

- **`GET /screenings` stays `[]`.** The two services aren't on the same Kafka broker (most common in
  dev mode — see §2 Path B), or you queried before the event was processed (wait ~1–2 s and retry).
  Confirm the message exists in Kafka UI and that `screening-service` logs show it consuming.
- **Connection refused on 8082.** Screening Service isn't running/published. In Docker, the
  `--profile apps` stack maps it to 8082 (added in `infra/compose.yaml`); in dev mode you must pass
  `-Dquarkus.http.port=8082`.
- **Port 8080 already in use.** Something else is on it (another Quarkus app, Structurizr). Stop it,
  or change `kycBaseUrl`.
- **`jq: command not found`.** Optional — drop the `| jq ...` and read the raw JSON.

## 8. Teardown

```bash
docker compose -f infra/compose.yaml --profile apps --profile observability down -v
# or: task down
```
