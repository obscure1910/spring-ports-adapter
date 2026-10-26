# Ports & Adapters Demo

Eine minimale Demonstration des **Ports & Adapters (Hexagonal Architecture)** Patterns mit Spring Boot und Java 25.

---

## Voraussetzungen

- Java 25
- Kein lokal installiertes Maven erforderlich: Der Maven Wrapper (`./mvnw`) wird verwendet.

---

## Starten über die CLI

### 1. Direkt mit dem Maven-Wrapper ausführen
```bash
# Mit Argument starten:
./mvnw spring-boot:run -Dspring-boot.run.arguments="Alice"
```

---

### 2. Als JAR bauen und ausführen

**Bauen:**
```bash
./mvnw clean package
```

**Starten:**
```bash
java -jar target/spring-ports-adapter-0.0.1-SNAPSHOT.jar Bob
```

---

## Verhalten

Beim Start mit einem Benutzernamen:

1. gibt die Anwendung eine Begrüßung aus,
2. zählt die Besuche während der aktuellen Programmlaufzeit,
3. meldet den Login an ein simuliertes Drittsystem über die Konsole.

Beispiel:

```text
Herzlich willkommen zum ersten Mal, Alice!
Drittsystem informiert: user=Alice, loggedInAt=2026-10-26T10:00:00Z
```

Die Besuchshistorie wird aktuell nur im Speicher gehalten und nach dem Beenden der Anwendung zurückgesetzt.

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
