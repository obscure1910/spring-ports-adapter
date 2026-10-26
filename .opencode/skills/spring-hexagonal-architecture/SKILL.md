---
name: spring-hexagonal-architecture
description: Use when designing, implementing, or reviewing a Spring Boot Ports-and-Adapters feature, especially when adding or replacing persistence layers or database adapters (H2, JDBC, JPA), application ports, inbound/outbound adapters, Spring Modulith module boundaries, or ArchUnit architecture tests.
---

# Spring Hexagonal Architecture

## Core rule

Dependencies point inward:

```text
adapter.in  → application.port.in
application → domain, application.port.in, application.port.out
adapter.out → application.port.out
```

The domain must not depend on Spring, JPA, HTTP, messaging clients, or adapters.

## Package layout

Use this layout for each business module:

```text
<module>
├── domain
├── application
│   └── port
│       ├── in
│       └── out
└── adapter
    ├── in
    └── out
```

Keep controller, CLI, scheduler, and message-consumer code in `adapter.in`. Keep JPA, HTTP-client, messaging-producer, and filesystem code in `adapter.out`.

## Feature workflow

1. Define one inbound port per use case in `application.port.in`.
2. Implement the use case in an application service.
3. Introduce an outbound port only for a capability outside the use case.
4. Implement each outbound port in a technology-specific adapter.
5. Keep transport validation and response mapping in inbound adapters.
6. Keep persistence mapping and external-system translation in outbound adapters.

## Transactions

Transaction boundaries belong to the application service that implements a write use case, because that layer coordinates the domain operation and all required outbound ports. If a use case writes persistent state, put `@Transactional` on the application-service method implementing the inbound port (or on that application-service class when every public use case is transactional).

Do not put `@Transactional` on JPA repositories, JPA adapters, or any other outbound adapter. An outbound adapter performs one technical operation; it must not decide how multiple operations form a business transaction. Do not call an external HTTP, email, or messaging system unreliably inside a database transaction; use an outbox or post-transaction delivery mechanism when reliable delivery matters.

## Durable counters and concurrent writes

For a database-backed counter, balance, inventory value, sequence, or other read-modify-write state, the observable contract includes concurrent calls, not only one sequential call. Keep the transaction boundary at the application write use case; the adapter executes the single persistence operation required by that use case.

The persistence adapter must make each increment atomic in the database. Use a database-side increment such as `SET count = count + 1`, or a database-specific atomic upsert whose update branch increments the stored column. For a first-write race, use an insert path that handles a duplicate-key collision by retrying the atomic update. Bind every external value as a SQL parameter.

Never use JVM-local coordination (`synchronized`, `Lock`, `ConcurrentHashMap`, or a process-local counter) as the correctness mechanism for state persisted in a database. It cannot protect concurrent requests across application instances, processes, test contexts, or future deployment changes. Never derive a replacement counter value from a subquery inside a `MERGE` unless the database guarantees that the statement is an atomic increment under the configured isolation level and this guarantee is covered by a concurrency test.

For any new durable write adapter, tests must cover:

- sequential creation and update of the same business key;
- concurrent calls for the same key, asserting no update is lost;
- database restart or fresh datasource lifecycle against the same file database;
- an input containing SQL-significant characters, proving parameter binding;
- default and alternative profile wiring, proving exactly one outbound-port implementation is active per profile.

## Module boundaries

Use Spring Modulith for module-level APIs:

- expose intentional APIs with `@NamedInterface`;
- restrict cross-module dependencies with `@ApplicationModule(allowedDependencies = ...)`;
- verify the complete graph with `ApplicationModules.of(Application.class).verify()`.

Use ArchUnit for restrictions inside a module that Modulith cannot express. Typical rules are:

- only one outbound adapter may access another module;
- inbound adapters may use application types only through inbound ports;
- no adapter may access another module's internal application, domain, or outbound packages.

Always use fully qualified package patterns. Do not use broad patterns such as `..notification..` when a module name can also occur in another package segment.

## Tests

Write tests for observable contracts:

| Layer | Test |
| --- | --- |
| Application service | Use-case result, state transition, outbound-port interaction |
| Inbound adapter | Input validation, request mapping, delegation to the inbound port |
| Outbound adapter | Translation to the external API or persistence model |
| Durable write adapter | Sequential, concurrent, restart, parameter-binding, and profile-wiring contracts |
| Module graph | `ApplicationModules.verify()` |
| Package graph | Focused ArchUnit rules for intentional boundaries |

Do not add tests solely for records, empty port interfaces, framework annotations, or trivial configuration. See [examples.md](examples.md) for Spring Boot examples.

## Common mistakes

- Controller calls an application service implementation instead of an inbound port.
- Application service imports JPA, HTTP, Kafka, or a class from another module.
- An adapter leaks an external DTO into the application or domain layer.
- A JPA or other outbound adapter defines the transaction boundary instead of the application service implementing the write use case.
- A JDBC/JPA adapter uses `synchronized` or another JVM-local lock to make a durable counter correct.
- An upsert reads a stored counter and writes back a derived value without proving its update is atomic under concurrent access.
- Persistence tests check only sequential calls and omit concurrent, restart, parameter-binding, or active-profile coverage.
- A broad ArchUnit pattern matches packages outside the intended module.
- Architecture tests check only module boundaries while package-level exceptions remain unprotected.

## Architecture compliance checklist

- [ ] Dependencies point inward: adapters depend on application ports, and application code depends only on domain types and application ports.
- [ ] Domain code imports no Spring, JPA, HTTP, messaging-client, or adapter types.
- [ ] Every use case is represented by an inbound port and implemented by an application service.
- [ ] Inbound adapters depend on inbound ports, never directly on application-service implementations.
- [ ] Outbound ports represent external capabilities; technology-specific adapters implement them.
- [ ] Transport validation and request/response mapping stay in inbound adapters.
- [ ] Persistence mapping and external-system translation stay in outbound adapters; external DTOs and persistence entities do not leak inward.
- [ ] Write-use-case transaction boundaries are owned by the application service, not by an outbound adapter.
- [ ] Durable counters and other read-modify-write state are atomically changed by the database, not protected by JVM-local locking.
- [ ] Durable write adapters are verified for concurrent updates, restart persistence, parameter binding, and exclusive profile wiring.
- [ ] Reliable external delivery after a database write uses an outbox or a post-transaction delivery mechanism.
- [ ] Cross-module APIs are intentional, declared with Spring Modulith, and protected by focused ArchUnit rules where needed.
- [ ] The module graph and intentional package-level boundary exceptions are covered by architecture tests.
