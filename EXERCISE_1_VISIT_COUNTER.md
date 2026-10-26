# Übungsaufgabe 1: Besuchszähler ("Schön dich wiederzusehen")

Dieses Dokument beschreibt eine Übungsaufgabe, um das **Ports & Adapters (Hexagonal Architecture)** Pattern anhand einer einfachen Datenhaltungslogik eigenständig zu vertiefen.

---

## 🎯 Ziel der Aufgabe

In der Ausgangsversion gibt `GreetingService` einfach `"Hello <name>!"` aus. Die Begrüßung soll davon abhängen, wie oft ein Benutzer die Anwendung bereits aufgerufen hat:

- **1. Besuch:**  
  `"Herzlich willkommen zum ersten Mal, Alice!"`
- **Ab dem 2. Besuch:**  
  `"Schön dich wiederzusehen, Alice! (Besuch #2)"`

---

## 💡 Architektur-Lerneffekt: Driven / Outbound Port für Datenhaltung

In der Hexagonalen Architektur darf der Kern (`application`/`domain`) **niemals direkt** wissen, wo Daten gespeichert werden (ob In-Memory, SQL-Datenbank, Redis oder Dateisystem).

Der Kern definiert lediglich einen **Outbound-Port (Interface)** mit den Methoden, die er fachlich benötigt. Die konkrete technische Speicherung erfolgt in einem **Outbound-Adapter**.

---

## 📋 Schritt-für-Schritt Anleitung

### Schritt 1: Outbound-Port erstellen
Erstelle im Package `greeting.application.port.out.persistence` (oder `greeting.application.port.out`) ein Interface:

```java
package de.obscure1910.pattern.ports.portsadapter.greeting.application.port.out;

public interface GreetingHistoryRepository {
    /**
     * Erhöht den Besuchszähler für den Benutzer um 1 und gibt den neuen Stand zurück.
     */
    int incrementAndGetVisits(String username);
}
```

### Schritt 2: Outbound-Adapter implementieren (In-Memory)
Erstelle im Adapter-Bereich `greeting.adapter.out.persistence` eine Implementierung für den Port:

```java
package de.obscure1910.pattern.ports.portsadapter.greeting.adapter.out.persistence;

import de.obscure1910.pattern.ports.portsadapter.greeting.application.port.out.GreetingHistoryRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryGreetingHistoryAdapter implements GreetingHistoryRepository {

    private final Map<String, Integer> visits = new ConcurrentHashMap<>();

    @Override
    public int incrementAndGetVisits(String username) {
        return visits.merge(username, 1, Integer::sum);
    }
}
```

### Schritt 3: Kernlogik im `GreetingService` anpassen
Integriere den neuen Port über Constructor Injection in den `GreetingService`:

1. `GreetingHistoryRepository` als `private final`-Feld hinzufügen und im Konstruktor übergeben.
2. Methode `sayHello(User user)` erweitern:
   - Anzahl der Besuche über `repository.incrementAndGetVisits(user.name())` abfragen.
   - Text formulieren:
     - Wenn `visits == 1`: `"Herzlich willkommen zum ersten Mal, <name>!"`
     - Wenn `visits > 1`: `"Schön dich wiederzusehen, <name>! (Besuch #<visits>)"`
   - Nachricht an `printer.print(...)` übergeben.

### Schritt 4: Tests schreiben & anpassen

1. **Unit-Test (`GreetingServiceTest`)**:
   - Mocke das `GreetingHistoryRepository` mit `@Mock`.
   - Testfall 1: Erster Besuch (`when(repository.incrementAndGetVisits("Alice")).thenReturn(1);`) → Prüfe, ob die Willkommensnachricht gedruckt wird.
   - Testfall 2: Wiederholter Besuch (`when(repository.incrementAndGetVisits("Alice")).thenReturn(2);`) → Prüfe, ob die Wiedersehensnachricht gedruckt wird.
   - *Wichtig:* Der Service-Test bleibt ein reiner Unit-Test ohne Spring-Kontext!

2. **Integrationstest (`PortsAdapterApplicationTests`)**:
   - Stelle sicher, dass `./mvnw test` komplett grün durchläuft.

---

## 🏆 Definition of Done (Selbstkontrolle)

- [ ] Das Interface liegt in `application.port.out` und hat keine Framework-Imports.
- [ ] Der Adapter liegt in `adapter.out` und implementiert das Interface.
- [ ] `GreetingService` hängt nur vom Interface ab, nicht von der Adapter-Klasse.
- [ ] `GreetingServiceTest` testet beide Fälle isoliert mit Mocks.
- [ ] `./mvnw test` läuft fehlerfrei durch.
