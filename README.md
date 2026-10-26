# Ports & Adapters Demo

Eine minimale Demonstration des **Ports & Adapters (Hexagonal Architecture)** Patterns mit Spring Boot und Java 25.

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
java -jar target/ports-adapter-0.0.1-SNAPSHOT.jar Bob
```

---

### Tests ausführen
```bash
./mvnw test
```

---

## 📚 Übungsaufgaben

1. **Besuchszähler:** Erweiterung um einen Persistence-Outbound-Port:
   👉 [`EXERCISE_1_VISIT_COUNTER.md`](EXERCISE_1_VISIT_COUNTER.md)
2. **Login-Benachrichtigung:** Neues Feature mit Benachrichtigung eines Drittsystems:
   👉 [`EXERCISE_2_LOGIN_NOTIFICATION.md`](EXERCISE_2_LOGIN_NOTIFICATION.md)
