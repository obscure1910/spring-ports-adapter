# REST Greeting Completion and Exercise Branches Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete Exercise 3’s REST and CLI behavior, verify it cleanly, and expose every completed exercise through a cumulative local Git branch.

**Architecture:** The REST controller and CLI remain inbound adapters for `SayHello`. `GreetingService` returns the greeting text and retains its notification side effect, while each inbound adapter is responsible for delivering that text through its own transport. Git branches are immutable learning checkpoints: Exercise 1 is the visit-counter solution, Exercise 2 builds on it with the notification architecture, and Exercise 3 builds on both with the REST adapter.

**Tech Stack:** Java 25, Spring Boot 4.1.1, Spring Web MVC, Spring Modulith 2.1.1, ArchUnit 1.4.2, JUnit 5, Mockito, MockMvc.

**Spec:** `EXERCISE_3_REST_GREETING.md` and the approved conversation requirements from 2026-10-26.

## Global Constraints

- `GET /greeting?name=Alice` returns HTTP 200 with a `text/plain` greeting.
- Missing, empty, or blank `name` returns HTTP 200 with `Wie heißt die zu grüßende Person?`.
- The CLI stays available only when `app.cli.enabled=true` is set; it prints the greeting returned by `SayHello`.
- `GreetingService` must not depend on `Printer`; delete obsolete printer production types.
- Keep Spring Modulith and the existing ArchUnit rules green.
- Use `spring-boot-starter-webmvc-test` with Maven test scope.
- Do not add `.archify/` to Git.
- Branches are cumulative: `exercise-1` → `exercise-2` → `exercise-3`.

## Review Focus

- A missing `name` must not be handled by Spring as HTTP 400; pin this with a MockMvc test in Task 1.
- A blank `name` must not call `SayHello`; pin this with `verifyNoInteractions(sayHello)` in Task 1.
- The CLI must display, rather than discard, the returned greeting; pin this through captured output in Task 2.
- Removing `Printer` must not break Spring constructor injection; pin this with the existing Spring context test and full clean suite in Task 2.
- Exercise branches must point to the intended historical checkpoints and form an ancestry chain; verify their SHAs and ancestry in Task 4.

---

### Task 1: Complete REST controller boundary tests

**Files:**
- Modify: `src/test/java/de/obscure1910/pattern/portsadapter/greeting/adapter/in/rest/GreetingRestControllerTest.java`

**Interfaces:**
- Consumes: `GreetingRestController.greeting(@RequestParam(required = false) String name)`.
- Produces: MVC tests for valid, missing, and blank query parameters.

- [ ] **Step 1: Add a test for a missing `name` parameter**

Use `GET /greeting`. Assert HTTP 200, `text/plain`, and the exact body `Wie heißt die zu grüßende Person?`; assert `verifyNoInteractions(sayHello)`.

- [ ] **Step 2: Add a test for a blank `name` parameter**

Use `GET /greeting?name=   `. Assert the same HTTP status and body as the missing-name case; assert `verifyNoInteractions(sayHello)`.

- [ ] **Step 3: Run the focused controller test**

Run: `./mvnw -Dtest=GreetingRestControllerTest test`

Expected: PASS. The controller already contains the required handling; these are regression tests for existing behavior.

### Task 2: Complete the CLI transition and remove the obsolete output port

**Files:**
- Modify: `src/main/java/de/obscure1910/pattern/portsadapter/PortsAdapterApplication.java`
- Modify: `src/main/java/de/obscure1910/pattern/portsadapter/greeting/application/GreetingService.java`
- Modify: `src/main/java/de/obscure1910/pattern/portsadapter/greeting/adapter/in/cli/GreetingCliAdapter.java`
- Delete: `src/main/java/de/obscure1910/pattern/portsadapter/greeting/application/port/out/console/Printer.java`
- Delete: `src/main/java/de/obscure1910/pattern/portsadapter/greeting/adapter/out/console/SystemOutPrinter.java`
- Modify: `src/test/java/de/obscure1910/pattern/portsadapter/greeting/adapter/in/cli/GreetingCliAdapterTest.java`
- Modify: `src/test/java/de/obscure1910/pattern/portsadapter/greeting/application/GreetingServiceTest.java`

**Interfaces:**
- Consumes: `String SayHello.sayHello(User user)`.
- Produces: a CLI adapter that writes that returned string with `IO.println(String)` and a `GreetingService` constructor with only history, notification, and time dependencies.

- [ ] **Step 1: Add a failing CLI-output test**

Use `OutputCaptureExtension` and `CapturedOutput`. Stub `sayHello.sayHello(new User("Alice"))` to return `Herzlich willkommen zum ersten Mal, Alice!`; call `adapter.run("Alice")`; assert that captured output contains the returned greeting.

- [ ] **Step 2: Run the focused CLI test to verify RED**

Run: `./mvnw -Dtest=GreetingCliAdapterTest test`

Expected: FAIL because `GreetingCliAdapter` currently discards the returned `String`.

- [ ] **Step 3: Implement the minimum transition**

In `GreetingCliAdapter.run`, assign the return value of `sayHello.sayHello(...)` and send it to `IO.println(...)`. Remove `Printer` from `GreetingService` imports and constructor, then delete `Printer` and `SystemOutPrinter`. Update `GreetingServiceTest` construction or Mockito injection to match the reduced constructor.

- [ ] **Step 4: Clean the application entry point**

Keep only `SpringApplication.run(PortsAdapterApplication.class, args);` in `main`. Remove the unused `ConfigurableApplicationContext` import, commented-out exit code, and explanatory comment.

- [ ] **Step 5: Run focused tests to verify GREEN**

Run: `./mvnw -Dtest=GreetingCliAdapterTest,GreetingServiceTest,PortsAdapterApplicationTests test`

Expected: PASS.

### Task 3: Align build metadata and user documentation

**Files:**
- Modify: `pom.xml`
- Modify: `README.md`
- Verify: `EXERCISE_3_REST_GREETING.md`

**Interfaces:**
- Produces: a runtime dependency graph free of web-test libraries and accurate run commands for REST and optional CLI modes.

- [ ] **Step 1: Limit Web MVC test support to tests**

Add `<scope>test</scope>` to the `spring-boot-starter-webmvc-test` dependency.

- [ ] **Step 2: Update README run instructions**

Document REST as the default mode at `http://localhost:8888/greeting?name=Alice`. Document the CLI alternative with both `--app.cli.enabled=true` and `--spring.main.web-application-type=none`; remove the outdated default CLI-only invocation.

- [ ] **Step 3: Check Exercise 3 against the final implementation**

Confirm its endpoint, HTTP-200 missing-name behavior, port `8888`, conditional CLI, and test checklist match the source. Correct only discovered mismatches.

- [ ] **Step 4: Validate documentation and Maven configuration**

Run: `git diff --check && ./mvnw help:effective-pom -Doutput=/tmp/opencode/effective-pom.xml`

Expected: no whitespace errors; the effective POM contains `spring-boot-starter-webmvc-test` with test scope.

### Task 4: Verify, commit Exercise 3, and create learning branches

**Files:**
- Verify: repository history and all staged Exercise 3 files.

**Interfaces:**
- Produces: one Exercise 3 commit on `main` and local branches `exercise-1`, `exercise-2`, and `exercise-3` at their final cumulative checkpoints.

- [ ] **Step 1: Run the complete clean test suite**

Run: `./mvnw clean test`

Expected: PASS with all unit, MVC, Spring context, Spring Modulith, and ArchUnit tests.

- [ ] **Step 2: Stage only intended files**

Stage all Exercise 3 source, tests, documentation, and README changes. Confirm `.archify/` remains untracked and unstaged with `git status --short`.

- [ ] **Step 3: Create the Exercise 3 commit**

Use a German Conventional Commit message describing the REST adapter, CLI transition, tests, and documentation.

- [ ] **Step 4: Create cumulative learning branches**

Create `exercise-1` at `e02a7ef`, `exercise-2` at `be0556c`, and `exercise-3` at the new Exercise 3 commit. Do not overwrite an existing branch without explicit user confirmation.

- [ ] **Step 5: Verify the branch checkpoints**

Run:

```bash
git merge-base --is-ancestor exercise-1 exercise-2
git merge-base --is-ancestor exercise-2 exercise-3
git log --oneline --decorate --all -6
```

Expected: both ancestry checks exit with status 0; each branch points to its intended exercise checkpoint.

## Self-Review

- **Spec coverage:** Tasks 1–3 cover all Exercise 3 Definition-of-Done items; Task 4 covers the requested local branch workflow.
- **Step scan:** Each implementation step names exact files, observable behavior, and focused commands.
- **Type consistency:** `SayHello.sayHello(User)` remains `String`; controller and CLI consume the same interface.
- **Review focus:** Missing and blank HTTP input, CLI output, Spring wiring after constructor cleanup, and branch ancestry each have explicit verification steps.
- **Proportion:** The plan focuses on the known incomplete Exercise 3 work and avoids unrelated architecture changes.
