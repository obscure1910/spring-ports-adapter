# Spring Hexagonal Architecture Examples

## 1. Persistence behind an outbound port

The application depends on a capability, not JPA:

```java
public interface VisitHistory {
    int incrementAndGetVisits(User user);
}
```

```java
@Service
class GreetingService implements SayHello {
    private final VisitHistory visitHistory;

    @Override
    @Transactional
    public String sayHello(User user) {
        int visits = visitHistory.incrementAndGetVisits(user);
        return visits == 1 ? "Welcome, " + user.name() : "Welcome back, " + user.name();
    }
}
```

A JPA adapter implements `VisitHistory`; it maps between entities and domain types without leaking JPA into `GreetingService`. The service owns the transaction because it owns the write use case; the adapter must not declare `@Transactional`.

## 2. Atomic durable counter adapter

The adapter does not coordinate concurrent callers with `synchronized`. The database owns atomic state transitions; the application service above owns the transaction boundary.

```java
class JdbcVisitHistory implements VisitHistory {
    private final JdbcTemplate jdbc;

    @Override
    public int incrementAndGetVisits(User user) {
        int updated = jdbc.update(
            "UPDATE visit_history SET visits = visits + 1 WHERE user_name = ?", user.name());
        if (updated == 0) {
            try {
                jdbc.update("INSERT INTO visit_history (user_name, visits) VALUES (?, 1)", user.name());
            } catch (DuplicateKeyException duplicate) {
                jdbc.update(
                    "UPDATE visit_history SET visits = visits + 1 WHERE user_name = ?", user.name());
            }
        }
        return jdbc.queryForObject(
            "SELECT visits FROM visit_history WHERE user_name = ?", Integer.class, user.name());
    }
}
```

Test the adapter with concurrent calls for one user, a fresh datasource against the same file database, and a name such as `Ada Lovelace's Test`. Assert every increment is retained; do not accept a passing sequential test as proof of write correctness.

## 3. REST as an inbound adapter

The controller handles HTTP only and delegates through the inbound port:

```java
@RestController
class GreetingController {
    private final SayHello sayHello;

    @GetMapping("/greeting")
    String greeting(@RequestParam String name) {
        return sayHello.sayHello(new User(name));
    }
}
```

Test it with `@WebMvcTest` and `@MockitoBean SayHello`; do not load persistence or other feature modules for a controller test.

## 4. A narrow cross-module adapter

Keep a foreign module out of the application service:

```text
orders.application
    → orders.application.port.out.payment

orders.adapter.out.payment
    → payment.application.port.in
```

Protect the exception with ArchUnit:

```java
classes()
    .that().resideInAPackage("com.example.orders..")
    .and().resideOutsideOfPackage("com.example.orders.adapter.out.payment..")
    .should().onlyDependOnClassesThat(
        resideOutsideOfPackage("com.example.payment..")
    );
```

Then add a second rule allowing the payment adapter to use only `com.example.payment.application.port.in..` from the payment module.

## 5. Modulith verification

Keep the module graph executable:

```java
@Test
void verifiesModuleStructure() {
    ApplicationModules.of(Application.class).verify();
}
```

This verifies cycles, module internals, and explicitly allowed dependencies. ArchUnit complements it with package-level rules.
