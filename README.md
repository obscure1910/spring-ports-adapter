# Ports & Adapters Demo

Eine minimale Demonstration des **Ports & Adapters (Hexagonal Architecture)** Patterns mit Spring Boot und Java 25.

---

## Voraussetzungen

- Java 25
- Kein lokal installiertes Maven erforderlich: Der Maven Wrapper (`./mvnw`) wird verwendet.

---

## Starten

### 1. REST-Schnittstelle starten

```bash
./mvnw spring-boot:run
```

Die Anwendung ist anschließend unter Port `8888` erreichbar:

```bash
curl "http://localhost:8888/greeting?name=Alice"
```

### 2. CLI als Alternative starten

```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.arguments="Alice --app.cli.enabled=true --spring.main.web-application-type=none"
```

---

### 3. Als JAR bauen und ausführen

**Bauen:**
```bash
./mvnw clean package
```

**Starten:**
```bash
java -jar target/spring-ports-adapter-0.0.1-SNAPSHOT.jar
```

---

## Verhalten

Bei einer Begrüßungsanfrage:

1. erzeugt die Anwendung eine Begrüßung,
2. zählt die Besuche dauerhaft in einer lokalen H2-Datenbank,
3. meldet den Login an ein simuliertes Drittsystem über die Konsole.

Beispiel:

```text
Herzlich willkommen zum ersten Mal, Alice!
Drittsystem informiert: user=Alice, loggedInAt=2026-10-26T10:00:00Z
```

Die Besuchshistorie wird standardmäßig dateibasiert in `data/ports-adapter` gespeichert und bleibt nach einem Neustart erhalten. Die erzeugten H2-Dateien sind nicht versioniert.

Für eine nur temporäre In-Memory-Historie kann das Profil `in-memory` aktiviert werden:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=in-memory
```

Die REST-Schnittstelle antwortet bei fehlendem oder leerem `name` mit HTTP 200 und `Wie heißt die zu grüßende Person?`.

---

## Architektur

Das Projekt demonstriert zwei fachliche Module:

- `greeting`: Begrüßung und Besuchshistorie.
- `notification`: Weitergabe eines Login-Ereignisses an ein Drittsystem.

Die fachliche Anwendung von `greeting` verwendet einen eigenen Outbound-Port. Nur der Adapter `greeting.adapter.out.notification` kennt den öffentlichen Inbound-Port von `notification`.

Die Grenzen werden automatisiert geprüft:

- **Spring Modulith** schützt die öffentlichen Modulschnittstellen.
- **ArchUnit** stellt sicher, dass nur `greeting.adapter.out.notification` auf `notification.application.port.in` zugreifen darf.

---

### Tests ausführen
```bash
./mvnw test
```

Der Testlauf enthält Unit-, Spring-Kontext-, Spring-Modulith- und ArchUnit-Tests.

---

## 📚 Übungsaufgaben

1. **Besuchszähler:** Erweiterung um einen Persistence-Outbound-Port:
   👉 [`EXERCISE_1_VISIT_COUNTER.md`](EXERCISE_1_VISIT_COUNTER.md)
2. **Login-Benachrichtigung:** Neues Feature mit Benachrichtigung eines Drittsystems:
   👉 [`EXERCISE_2_LOGIN_NOTIFICATION.md`](EXERCISE_2_LOGIN_NOTIFICATION.md)
3. **REST-Begrüßung:** Alternativer Inbound-Adapter mit einem REST-Controller:
   👉 [`EXERCISE_3_REST_GREETING.md`](EXERCISE_3_REST_GREETING.md)
