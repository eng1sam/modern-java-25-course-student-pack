// Day 3 — Rosetta Stone: WORKED SOLUTION (Java 25).
//
// Run with:   java RosettaStone.java
//   (a "compact source file" — no class wrapper, no `public static void main`.
//    `void main()` and `IO.println` are final in JDK 25.)
//
// This is one worked answer; yours may differ. What matters is the day's rules:
// illegal states unrepresentable, Optional over null, an exhaustive switch with
// NO default, and NO String Templates (use formatted(...), STR."..." does not exist).

import java.util.List;
import java.util.Map;
import java.util.Optional;

// TODO 1 — Records: an immutable value object whose compact constructor rejects
//          negative cents, so a negative Money can never be built.
record Money(long cents, String currency) {
    Money {
        if (cents < 0) throw new IllegalArgumentException("cents must be >= 0, was " + cents);
    }
}

// TODO 2 — Sealed type: a CLOSED set of cases.
sealed interface OrderEvent permits OrderPlaced, OrderPaid, OrderCancelled {}
record OrderPlaced(String sku, int quantity) implements OrderEvent {}
record OrderPaid(String txnId)               implements OrderEvent {}
record OrderCancelled(String reason)         implements OrderEvent {}

// TODO 3 — Pattern matching: an EXHAUSTIVE switch with record patterns and NO
//          default. Delete a case and the compiler refuses to build — that is the point.
String describe(OrderEvent event) {
    return switch (event) {
        case OrderPlaced(var sku, var qty) -> "placed %d x %s".formatted(qty, sku);
        case OrderPaid(var txnId)          -> "paid (txn %s)".formatted(txnId);
        case OrderCancelled(var reason)    -> "cancelled: %s".formatted(reason);
    };
}

// TODO 4 — Text block + formatted (no String Templates).
String orderJson(OrderPlaced o) {
    return """
        { "sku": "%s", "qty": %d }
        """.formatted(o.sku(), o.quantity());
}

// TODO 5 — Stream: a lazy filter/map pipeline with a terminal sum.
int totalUnits(List<OrderPlaced> orders) {
    return orders.stream()
            .filter(o -> o.quantity() > 0)
            .mapToInt(OrderPlaced::quantity)
            .sum();
}

// TODO 6 — Optional: look up a role and fall back, with no null check.
String roleFor(Map<String, String> roles, String user) {
    return Optional.ofNullable(roles.get(user)).orElse("guest");
}

void main() {
    IO.println("Rosetta Stone — worked solution.");
    IO.println("Checklist:");
    IO.println("  [x] TODO 1  record Money + validation");
    IO.println("  [x] TODO 2  sealed OrderEvent + 3 records");
    IO.println("  [x] TODO 3  describe(): exhaustive switch, no default");
    IO.println("  [x] TODO 4  orderJson(): text block + formatted");
    IO.println("  [x] TODO 5  totalUnits(): stream pipeline");
    IO.println("  [x] TODO 6  roleFor(): Optional, no null");
    IO.println("");

    List<String> currencies = List.of("USD", "EUR", "GBP");
    Optional<String> eur = currencies.stream().filter("EUR"::equals).findFirst();
    IO.println("warm-up — first EUR: " + eur.orElse("none"));

    var price = new Money(1299, "USD");
    IO.println("TODO 1 — " + price);
    IO.println("TODO 3 — " + describe(new OrderPlaced("BOOK-1", 3)));
    IO.println("TODO 3 — " + describe(new OrderCancelled("out of stock")));
    IO.print  ("TODO 4 — " + orderJson(new OrderPlaced("BOOK-1", 3)));
    IO.println("TODO 5 — total units: "
            + totalUnits(List.of(new OrderPlaced("A", 3), new OrderPlaced("B", 5))));
    IO.println("TODO 6 — role: " + roleFor(Map.of("ada", "admin"), "guest"));

    // Proof the compact constructor makes illegal state unrepresentable:
    try {
        var bad = new Money(-1, "USD");
        IO.println("TODO 1 — built a negative Money?! " + bad);   // unreachable
    } catch (IllegalArgumentException e) {
        IO.println("TODO 1 — rejected negative Money: " + e.getMessage());
    }
}
