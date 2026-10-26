# Greeting–Notification Hexagonal Port Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Decouple `GreetingService` from Notification's input API by introducing a Greeting-owned outbound notification port and an adapter that delegates to Notification's published input port.

**Architecture:** `GreetingService` depends only on a port defined under `greeting.application.port.out.notification`. A Spring component in `greeting.adapter.out.notification` implements that port and translates the Greeting-owned notification value object into Notification's `LoginNotificationCommand` before invoking `NotifyUserLogin`. The existing Spring Modulith dependency `notification :: in` remains, but it is confined to the adapter.

**Tech Stack:** Java 25, Spring Boot 4.1.1, Spring Modulith 2.1.1, JUnit 5, Mockito.

**Spec:** Conversation requirement from 2026-10-26: implement the previously recommended strict hexagonal Greeting-to-Notification integration.

## Global Constraints

- Do not alter unrelated uncommitted exercise work.
- Keep `notification :: in` as the only explicitly allowed external dependency of `greeting`.
- `GreetingService` must not import any type from the `notification` module.
- Keep Spring component wiring automatic through constructor injection and `@Component`.

## Review Focus

- The adapter must preserve both username and timestamp while translating between the two commands; test this with literal values.
- `GreetingService` must retain the notification side effect after moving to the local outbound port.
- The application service must not couple to Notification types; the Modulith architecture test must remain green.
- Existing first-visit and repeat-visit greeting output must not regress.

---

### Task 1: Greeting-owned notification output contract

**Files:**
- Create: `src/main/java/de/obscure1910/pattern/ports/portsadapter/greeting/application/port/out/notification/UserLoginNotification.java`
- Create: `src/main/java/de/obscure1910/pattern/ports/portsadapter/greeting/application/port/out/notification/UserLoginNotifier.java`
- Modify: `src/test/java/de/obscure1910/pattern/ports/portsadapter/greeting/application/GreetingServiceTest.java`
- Modify: `src/main/java/de/obscure1910/pattern/ports/portsadapter/greeting/application/GreetingService.java`

**Interfaces:**
- Produces: `UserLoginNotification(String username, Instant loggedInAt)` and `UserLoginNotifier.notify(UserLoginNotification notification)`.
- Consumes: `CurrentTimeProvider.now()` from the existing Greeting-owned output port.

- [ ] **Step 1: Write a failing GreetingService test**

Use a mocked `UserLoginNotifier` and fixed `Instant.parse("2026-10-26T10:00:00Z")`. Assert that `sayHello(new User("Alice"))` sends `new UserLoginNotification("Alice", fixedInstant)`.

- [ ] **Step 2: Run the focused test to verify RED**

Run: `./mvnw -Dtest=GreetingServiceTest test`

Expected: compilation failure because the Greeting-owned notification port does not yet exist.

- [ ] **Step 3: Implement the minimal Greeting-owned contract and use it in GreetingService**

Create the record and interface stated above. Replace Notification imports and `NotifyUserLogin` in `GreetingService` with `UserLoginNotifier`; construct the Greeting-owned record from the user name and current time.

- [ ] **Step 4: Run the focused test to verify GREEN**

Run: `./mvnw -Dtest=GreetingServiceTest test`

Expected: PASS.

### Task 2: Notification module integration adapter

**Files:**
- Create: `src/main/java/de/obscure1910/pattern/ports/portsadapter/greeting/adapter/out/notification/NotificationModuleUserLoginNotifier.java`
- Create: `src/test/java/de/obscure1910/pattern/ports/portsadapter/greeting/adapter/out/notification/NotificationModuleUserLoginNotifierTest.java`

**Interfaces:**
- Consumes: `UserLoginNotifier.notify(UserLoginNotification notification)` and Notification's published `NotifyUserLogin.notify(LoginNotificationCommand command)`.
- Produces: a Spring `@Component` implementation of `UserLoginNotifier`.

- [ ] **Step 1: Write a failing adapter test**

For `new UserLoginNotification("Alice", Instant.parse("2026-10-26T10:00:00Z"))`, verify the injected `NotifyUserLogin` receives `new LoginNotificationCommand("Alice", sameInstant)`.

- [ ] **Step 2: Run the focused test to verify RED**

Run: `./mvnw -Dtest=NotificationModuleUserLoginNotifierTest test`

Expected: compilation failure because the adapter does not exist.

- [ ] **Step 3: Implement the adapter**

Add a constructor-injected `NotifyUserLogin`, annotate the class with `@Component`, and translate the local notification record into `LoginNotificationCommand` before delegation.

- [ ] **Step 4: Run the focused test to verify GREEN**

Run: `./mvnw -Dtest=NotificationModuleUserLoginNotifierTest test`

Expected: PASS.

### Task 3: Verify complete architecture and suite

**Files:**
- Verify: `src/test/java/de/obscure1910/pattern/ports/portsadapter/ApplicationTests.java`

**Interfaces:**
- Consumes: the completed Greeting port and adapter.
- Produces: verified modular and test-suite integrity.

- [ ] **Step 1: Run the full Maven test suite**

Run: `./mvnw test`

Expected: all tests pass, including `ApplicationTests`; the generated diagrams under `target/spring-modulith-docs/` show the allowed `greeting` to `notification` dependency.
