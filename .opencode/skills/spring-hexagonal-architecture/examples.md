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

## 2. REST as an inbound adapter

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

## 3. A narrow cross-module adapter

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

## 4. Modulith verification

Keep the module graph executable:

```java
@Test
void verifiesModuleStructure() {
    ApplicationModules.of(Application.class).verify();
}
```

This verifies cycles, module internals, and explicitly allowed dependencies. ArchUnit complements it with package-level rules.
