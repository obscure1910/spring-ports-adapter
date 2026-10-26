---
name: spring-hexagonal-architecture
description: Use when designing, implementing, or reviewing Spring Boot features with Ports and Adapters, application ports, inbound or outbound adapters, Spring Modulith module boundaries, or ArchUnit architecture tests.
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

An application service may use `@Transactional` when it owns a write use case. Do not put transaction boundaries in JPA adapters. Do not call an external HTTP, email, or messaging system unreliably inside a database transaction; use an outbox or post-transaction delivery mechanism when reliable delivery matters.

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
| Module graph | `ApplicationModules.verify()` |
| Package graph | Focused ArchUnit rules for intentional boundaries |

Do not add tests solely for records, empty port interfaces, framework annotations, or trivial configuration. See [examples.md](examples.md) for Spring Boot examples.

## Common mistakes

- Controller calls an application service implementation instead of an inbound port.
- Application service imports JPA, HTTP, Kafka, or a class from another module.
- An adapter leaks an external DTO into the application or domain layer.
- A broad ArchUnit pattern matches packages outside the intended module.
- Architecture tests check only module boundaries while package-level exceptions remain unprotected.
