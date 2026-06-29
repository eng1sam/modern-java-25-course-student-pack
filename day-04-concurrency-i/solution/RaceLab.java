// Day 4 — Concurrency I: WORKED SOLUTION.
// Run with:  java RaceLab.java        (single-file source program — no build needed)
//
// Part A fills in the virtual-thread executor (blocking is cheap).
// Part B reproduces the race ON PURPOSE — the Counter stays UNSAFE. Today's goal is
// to SEE updates get lost; making it correct is Day 5.

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class RaceLab {

    static final int THREADS = 10_000;             // try bumping toward 1_000_000 in Part A
    static final int INCREMENTS_PER_THREAD = 1_000;

    public static void main(String[] args) throws InterruptedException {
        partA_cheapBlocking();
        System.out.println();
        partB_seeARace();
    }

    // Part A: blocking is cheap again. Every task sleeps (blocks!) 100 ms, then tallies completion.
    static void partA_cheapBlocking() throws InterruptedException {
        AtomicLong done = new AtomicLong();   // safe counter — just to confirm every task ran

        long start = System.nanoTime();

        // TODO 1 (done): one virtual thread per task; closing the executor waits for all.
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < THREADS; i++) {
                executor.submit(() -> {
                    Thread.sleep(100);
                    done.incrementAndGet();
                    return null;
                });
            }
        } // executor.close() blocks until every task has finished

        long millis = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("Part A: %,d virtual threads, each blocked 100 ms — finished in %,d ms%n",
                THREADS, millis);
        System.out.printf("        completed = %,d (expected %,d)%n", done.get(), (long) THREADS);
        System.out.println("        (run one-at-a-time this would take "
                + (THREADS / 10) + " seconds — blocking is cheap now)");
    }

    // Part B: a data race. Many threads do value++ on a SHARED, unguarded field.
    static void partB_seeARace() throws InterruptedException {
        Counter counter = new Counter();
        long expected = (long) THREADS * INCREMENTS_PER_THREAD;

        // TODO 2 (done): each of THREADS virtual threads increments the SAME counter
        //                INCREMENTS_PER_THREAD times, with no synchronisation.
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < THREADS; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                        counter.increment();
                    }
                    return null;
                });
            }
        }

        long lost = expected - counter.value;
        System.out.printf("Part B: expected %,d but counter = %,d%n", expected, counter.value);
        System.out.println(lost == 0
                ? "        (no lost updates THIS run — re-run, or raise THREADS; the race is still there)"
                : String.format("        lost %,d updates ↑ that gap is a RACE CONDITION. Day 5 makes shared state safe.", lost));
    }

    // A deliberately UNSAFE counter: value++ is read-modify-write — three steps, not one.
    // Two threads can read the same value, both add one, and both write back the same
    // result — so one increment is silently lost. That is why Part B comes up short.
    static final class Counter {
        long value = 0;                  // not volatile, not atomic, not synchronized — on purpose
        void increment() { value++; }    // NOT atomic: read value, add 1, write value
    }
}
