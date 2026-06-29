# Day 06 — Build an order database by hand with `psql`

**Goal:** talk to PostgreSQL directly — no framework in between. Connect with `psql` *inside* the
container, build `customers` and `orders` by hand with the right types and constraints, relate
them with a foreign key, load data, and answer real questions with `SELECT` / `JOIN` / `GROUP BY`.
Watch constraints reject bad data, read an `EXPLAIN`, prove a `ROLLBACK`, query `jsonb`, take a backup.

> No starter. You practise on the **real Postgres container**. No Hibernate, Panache,
> Flyway or Quarkus today.

## Connect

```bash
docker compose -f infra/compose.yaml up -d postgres                            # start Postgres
docker compose -f infra/compose.yaml exec postgres psql -U orders -d orders   # -> orders=#
```

Look around: `\l` (databases) · `\dt` (tables) · `\d orders` (describe) · `\timing on`.

## Do this

1. `CREATE TABLE orders (…)` with `bigint GENERATED ALWAYS AS IDENTITY` PK, `CHECK (quantity > 0)`,
   a `status` `CHECK (… IN (…))`, a `jsonb attributes` column, and `timestamptz` defaults.
2. Insert good rows; insert one with `quantity = 0` and watch the `CHECK` reject it.
3. Query: `WHERE`, `ORDER BY`, `LIMIT`, `count(*)`.
4. Add `customers`, then `ALTER TABLE orders ADD CONSTRAINT … FOREIGN KEY …`; insert an order for
   a non-existent customer and watch the FK reject it.
5. `JOIN` orders to customers; `GROUP BY` for spend per customer.
6. `EXPLAIN` a filter, `CREATE INDEX`, `EXPLAIN` again — practise **reading the plan**.
7. `BEGIN; UPDATE orders SET status='CANCELLED'; … ROLLBACK;` — prove all-or-nothing.
8. Insert a row with `attributes` JSON and query inside it with `->>` and `@>`.
9. Back up: `docker compose -f infra/compose.yaml exec -T postgres pg_dump -U orders orders > orders-backup.sql`.

## Done when

- [ ] You reached the `orders=#` prompt *inside* the container.
- [ ] `orders` and `customers` exist with their constraints.
- [ ] You saw a `CHECK` **and** a `FOREIGN KEY` reject bad data.
- [ ] A `JOIN` + `GROUP BY` answered "spend per customer".
- [ ] A `ROLLBACK` undid a change no later `SELECT` could see.
- [ ] `orders-backup.sql` exists and contains readable SQL.

> The `orders` table you write here is the same one Flyway creates for you on **Day 9**.

Full brief: `labs/day-06/README.md` · Concepts: `docs/content/day-06/`.
