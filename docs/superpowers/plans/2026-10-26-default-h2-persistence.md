# Default H2 Persistence Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persist greeting visit counts in a file-backed H2 database by default instead of only keeping them in memory.

**Architecture:** The application continues to depend only on `GreetingHistoryRepository`. A JDBC outbound adapter implements that port using Spring's `JdbcTemplate`; the current in-memory adapter is retained behind an explicit `in-memory` profile. Spring Boot configures a file-backed H2 `DataSource` and creates the required table from `schema.sql`.

**Tech Stack:** Java 25, Spring Boot 4.1.1, Spring JDBC, H2, JUnit 5.

**Spec:** Inline user request: use H2 as the default database instead of an in-memory-only store.

## Global Constraints

- The default database is a persistent H2 file database.
- `GreetingService` remains independent from JDBC and H2 classes.
- The existing in-memory implementation remains available only when the `in-memory` Spring profile is enabled.

## Review Focus

- A process restart must retain a user's previously incremented visit counter.
- The first greeting for a user must start at visit `1`.
- A second greeting for the same user must return visit `2`.
- Names containing spaces or quotes must be stored as values, never interpolated into SQL.
- Normal application startup must have exactly one `GreetingHistoryRepository` bean.

---

### Task 1: H2-backed greeting-history adapter

**Files:**
- Create: `src/main/java/de/obscure1910/pattern/portsadapter/greeting/adapter/out/persistence/JdbcGreetingHistoryAdapter.java`
- Create: `src/main/resources/schema.sql`
- Modify: `pom.xml`
- Modify: `src/main/resources/application.yaml`
- Modify: `src/main/java/de/obscure1910/pattern/portsadapter/greeting/adapter/out/persistence/InMemoryGreetingHistoryAdapter.java`
- Test: `src/test/java/de/obscure1910/pattern/portsadapter/greeting/adapter/out/persistence/JdbcGreetingHistoryAdapterTest.java`

**Interfaces:**
- Consumes: `GreetingHistoryRepository#incrementAndGetVisits(User): int`.
- Produces: A default Spring bean implementing `GreetingHistoryRepository` through JDBC.

- [ ] **Step 1: Write the failing adapter test**

```java
@Test
void incrementsVisitsForTheSameUser() {
    assertThat(adapter.incrementAndGetVisits(new User("Alice"))).isEqualTo(1);
    assertThat(adapter.incrementAndGetVisits(new User("Alice"))).isEqualTo(2);
}
```

- [ ] **Step 2: Run the adapter test to verify it fails**

Run: `./mvnw -Dtest=JdbcGreetingHistoryAdapterTest test`
Expected: FAIL because `JdbcGreetingHistoryAdapter` does not exist.

- [ ] **Step 3: Add H2 and JDBC dependencies plus schema and default datasource configuration**

Configure `spring.datasource.url` as `jdbc:h2:file:./data/ports-adapter;DB_CLOSE_ON_EXIT=FALSE`; initialize the `greeting_history(user_name, visit_count)` table on startup.

- [ ] **Step 4: Implement `JdbcGreetingHistoryAdapter#incrementAndGetVisits(User): int`**

Use parameterized JDBC operations and a database transaction to insert the first count or increment and return the current count. Activate this adapter unless profile `in-memory` is active; activate the old in-memory adapter only for that profile.

- [ ] **Step 5: Run the adapter test to verify it passes**

Run: `./mvnw -Dtest=JdbcGreetingHistoryAdapterTest test`
Expected: PASS.

### Task 2: Default wiring and documentation

**Files:**
- Modify: `src/test/java/de/obscure1910/pattern/portsadapter/PortsAdapterApplicationTests.java`
- Modify: `README.md`
- Modify: `.gitignore`

**Interfaces:**
- Consumes: The default `GreetingHistoryRepository` bean from Task 1.
- Produces: A documented, ignored persistent H2 database location.

- [ ] **Step 1: Write the failing Spring-context test**

```java
assertThat(greetingHistoryRepository).isInstanceOf(JdbcGreetingHistoryAdapter.class);
```

- [ ] **Step 2: Run the context test to verify it fails**

Run: `./mvnw -Dtest=PortsAdapterApplicationTests test`
Expected: FAIL while the default bean remains the in-memory adapter.

- [ ] **Step 3: Document the database behavior and ignore generated H2 files**

State that the default database resides under `data/ports-adapter` and survives restarts; provide the `in-memory` profile command as an opt-in alternative.

- [ ] **Step 4: Run focused tests and the complete suite**

Run: `./mvnw test`
Expected: PASS.
