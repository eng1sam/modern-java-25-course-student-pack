# Day 04 — Cheap threads, and a race you can see

**Goal:** feel both sides of concurrency in one program. First prove a flood of *blocking*
virtual threads costs almost nothing; then reproduce a **race condition** on a shared counter and
watch updates vanish. Today's job is to make the race **fail reproducibly** — fixing it is Day 5.

> Starter: `starter/RaceLab.java`. Single-file, no build, no preview flag:
> `java starter/RaceLab.java`. Virtual threads are **final** in Java 25.

## Do this

Run it as-is first — the TODOs are unfinished, so both counts come out wrong (that's expected).

1. **Part A — cheap blocking.** Complete TODO 1: open a `newVirtualThreadPerTaskExecutor()` and
   submit `THREADS` tasks, each doing `Thread.sleep(100)` then `done.incrementAndGet()`. Re-run:
   every task finishes, and wall-clock is a small multiple of 100 ms — **not** `THREADS × 100 ms`.
2. **Part B — see a race.** Complete TODO 2: launch `THREADS` virtual threads, each calling
   `counter.increment()` `INCREMENTS_PER_THREAD` times.
3. Re-run a few times. `counter.value` comes out **less than** expected, differently each run —
   lost updates.
4. Read TODO 3 in `Counter` and be able to say in one sentence why `value++` is not atomic.

## Done when

- [ ] `java starter/RaceLab.java` runs (single-file, no `--enable-preview`).
- [ ] Part A: all `THREADS` tasks complete, far faster than one-at-a-time.
- [ ] Part B: a **reproducible** lost-update race (`counter.value < expected`).
- [ ] You can explain why `value++` loses updates across threads.

## Solution

A worked answer is in `solution/RaceLab.java` (`java solution/RaceLab.java`). Note it still
**loses updates** — Day 4's goal is to *see* the race, not fix it (that's Day 5). Try it yourself first.

## Going further

Push `THREADS` toward `1_000_000` and watch your laptop shrug · swap in
`Executors.newFixedThreadPool(200)` and watch it get *worse* · preview Day 5: replace `long value`
with an `AtomicLong` and the losses vanish.

Full brief: `labs/day-04/README.md` · Concepts: `docs/content/day-04/`.
