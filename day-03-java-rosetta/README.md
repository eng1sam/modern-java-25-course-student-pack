# Day 03 — Java for polyglots (the Rosetta Stone)

**Goal:** translate the idioms you reach for in C#, TypeScript, Python, Ruby or Go into
idiomatic **Java 25** — records, a sealed type, an exhaustive `switch`, a text block, a Stream,
and an `Optional` — with no `null` as control flow and **no** String Templates.

> Starter: `starter/RosettaStone.java`. It's a **compact source file** — run it directly, no
> build: `java starter/RosettaStone.java`. (Compact source + implicit `main` are final in JDK 25.)

## Do this

Open `starter/RosettaStone.java`. It prints a checklist; each `// TODO` is one idiom. Implement
in order, re-running after each:

1. **Records** — `Money(long cents, String currency)` with a *compact constructor* that rejects
   negative cents. Make illegal states unrepresentable.
2. **Sealed type** — `sealed interface OrderEvent permits OrderPlaced, OrderPaid, OrderCancelled`.
3. **Exhaustive switch** — `describe(OrderEvent)` with record patterns and **no `default`**.
   Delete a `case`, watch it stop compiling, put it back.
4. **Text block** — build multi-line JSON with a text block + `formatted(...)`. Never `STR."..."`.
5. **Stream** — total quantity over a list of `OrderPlaced` via filter/map/reduce.
6. **Optional** — look up a currency with a fallback, no `null` check.

Wire each finished TODO into the run (uncomment the demo lines) and re-run. Then pair-review:
any stray `null` or `default`?

## Done when

- [ ] `java starter/RosettaStone.java` compiles and runs.
- [ ] A record with a validating compact constructor (a negative `Money` cannot be built).
- [ ] A sealed interface consumed by an exhaustive `switch` (no `default`).
- [ ] One text block with `formatted(...)`; **no** String Templates anywhere.
- [ ] One Stream pipeline and one `Optional` instead of a `null` check.

## Solution

A worked answer is in `solution/RosettaStone.java` — run it the same way
(`java solution/RosettaStone.java`). Try every TODO yourself first.

## Going further

Add a guarded pattern (`when`) and a nested record pattern · compute an expiry `Instant` with
`java.time` · rewrite it as a classic `public class` with `main` and note the ceremony removed.

Full brief: `labs/day-03/README.md` · Concepts: `docs/content/day-03/`.
