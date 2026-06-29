# Day 02 — Docker & Compose

**Goal:** bring up the course stack (PostgreSQL + Kafka + Kafka UI) with one command, step
*inside* a running container, and prove that a named volume outlives the container.

> No starter. You practise on the **real course stack** (`infra/compose.yaml`).

## Prerequisites

- Docker with Compose V2 (`docker version` shows a Client **and** a Server).
- If you already run Postgres on 5432: `export POSTGRES_HOST_PORT=5433` before you bring the stack up.

> Every command runs from the **repo root**. The stack is one Compose file, `infra/compose.yaml`.

## Do this

1. Confirm Docker: `docker run --rm hello-world`.
2. See an image is layers: `docker pull postgres:16`, then `docker history postgres:16`.
3. Bring up the stack:
   ```bash
   docker compose -f infra/compose.yaml up -d postgres kafka kafka-ui
   ```
   then `docker compose -f infra/compose.yaml ps` — all three `Up`, `postgres` `healthy`. Kafka UI
   at <http://localhost:8081>.
4. Step inside the database (the Day 6 move):
   ```bash
   docker compose -f infra/compose.yaml exec postgres psql -U orders -d orders
   ```
   `CREATE TABLE persistence_demo (note text);` then insert a row.
5. Prove the volume persists: tear the containers down but keep volumes —
   `docker compose -f infra/compose.yaml down` (no `-v`) — then bring them back with the same
   `up -d` command above. The row is still there. Now tear down **with** volumes —
   `docker compose -f infra/compose.yaml down -v` — bring it up again, and the table is gone.
6. Map it to the file: open `infra/compose.yaml` and name each service's image, ports, the
   `pgdata` volume, and the `postgres` healthcheck. Know why a service uses `postgres:5432`
   while your laptop uses `localhost:5432`.

## Done when

- [ ] `ps` shows all three services `Up`, `postgres` `healthy`.
- [ ] You opened a shell *inside* a container with `docker compose exec`.
- [ ] A row survived `down` (no `-v`) + `up -d`, and was wiped by `down -v` —
      and you can explain why.

## Going further

Override the host port and confirm with `ps` · read the real multi-stage `apps/order-service/Dockerfile`.

Full brief: `labs/day-02/README.md` · Concepts: `docs/content/day-02/`.
