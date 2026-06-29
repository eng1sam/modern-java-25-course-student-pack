# Day 05 — Make it safe, then make it structured

**Goal:** fix the race from Day 4 so the counter is correct on **every** run, then group two
concurrent calls into one unit of work with a `StructuredTaskScope` that combines both results and
cancels the sibling if either fails.

> Starter: `starter/RaceLab.java`. Part 2 uses the **preview** `StructuredTaskScope` API, so this
> one needs both flags:
> ```bash
> java --enable-preview --source 25 starter/RaceLab.java
> ```
> Virtual threads and scoped values are final — only structured concurrency needs the flag.

## Do this

1. Run as-is and watch the race: `safe ->` (well, `race ->`) prints a number **below** 1000,
   different each run.
2. **TODO 1 — make it safe.** Implement `runSafe()` to return **exactly** `THREADS` every time.
   Pick one and make it correct: a `synchronized` block, an `AtomicInteger.incrementAndGet()`, or
   a `ConcurrentHashMap.merge(key, 1L, Long::sum)`. (Not `volatile` alone.)
3. **TODO 2 — make it structured.** In `fetchQuote(...)`, fork a second subtask for
   `fetchPriceCents(sku)`, `join()` the scope, then combine both into a `Quote`.
4. Re-run a few times: `safe ->` is `1000` every time; `quote ->` prints a combined `Quote[…]`.

## Done when

- [ ] `runSafe()` returns exactly `THREADS` on every run (no wobble).
- [ ] The fix uses `synchronized`, an atomic, or a concurrent collection.
- [ ] `fetchQuote(...)` forks both subtasks and combines them after `join()`.
- [ ] If one subtask throws, the scope cancels the other and `join()` propagates the failure.

## Solution

A worked answer is in `solution/RaceLab.java`
(`java --enable-preview --source 25 solution/RaceLab.java`). Try both TODOs yourself first.

## Going further

Make one fetch throw and confirm the sibling is cancelled · swap `AtomicInteger` for `LongAdder`
and reason about the trade-off · bind a `ScopedValue` correlation id and read it inside a subtask.

Full brief: `labs/day-05/README.md` · Concepts: `docs/content/day-05/`.
