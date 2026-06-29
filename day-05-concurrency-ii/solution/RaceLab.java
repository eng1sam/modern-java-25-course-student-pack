// Day 5 — Concurrency II: WORKED SOLUTION.
//
// Part 1: make the racing counter correct (here: an AtomicInteger).
// Part 2: treat two slow calls as ONE unit of work with a StructuredTaskScope.
//
// Structured concurrency is a PREVIEW feature in JDK 25, so run with:
//     java --enable-preview --source 25 RaceLab.java

import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.concurrent.atomic.AtomicInteger;

public class RaceLab {

    static final int THREADS = 1_000;

    // ---- Part 1: the race (Day 4's cliffhanger) ---------------------------

    static int unsafeCounter = 0;

    static void bumpUnsafe() {
        unsafeCounter++;            // read-modify-write: three steps, not one
    }

    static int runRace() {
        unsafeCounter = 0;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < THREADS; i++) {
                executor.submit(RaceLab::bumpUnsafe);
            }
        } // executor.close() waits for every task to finish
        return unsafeCounter;
    }

    // TODO 1 (done): an AtomicInteger makes increment a single atomic compare-and-set,
    // so no update is ever lost — runSafe() returns exactly THREADS every run.
    // (synchronized on a private lock, or ConcurrentHashMap.merge, would work equally.)
    static int runSafe() {
        AtomicInteger counter = new AtomicInteger();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < THREADS; i++) {
                executor.submit(counter::incrementAndGet);
            }
        }
        return counter.get();
    }

    // ---- Part 2: structured concurrency (PREVIEW) ------------------------

    record Quote(int stock, int priceCents) {}

    static int fetchStock(String sku) throws InterruptedException {
        Thread.sleep(200);          // pretend this is a slow network call
        return 42;
    }

    static int fetchPriceCents(String sku) throws InterruptedException {
        Thread.sleep(200);          // and so is this — they should run in parallel
        return 1999;
    }

    // Fan out two slow calls and combine them as ONE unit of work: if either subtask
    // fails, the scope cancels the other and join() throws. Both run in parallel, so
    // the whole call takes ~200 ms, not ~400 ms.
    static Quote fetchQuote(String sku) throws InterruptedException {
        try (var scope = StructuredTaskScope.open()) {     // default joiner: all must succeed
            Subtask<Integer> stock = scope.fork(() -> fetchStock(sku));

            // TODO 2 (done): fork the second subtask, join, then combine both results.
            Subtask<Integer> price = scope.fork(() -> fetchPriceCents(sku));
            scope.join();
            return new Quote(stock.get(), price.get());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("race  -> " + runRace() + "   (expected " + THREADS + ", usually less)");
        System.out.println("safe  -> " + runSafe() + "   (must be exactly " + THREADS + ")");
        System.out.println("quote -> " + fetchQuote("SKU-1"));
    }
}
