# Übungsaufgabe 3: Begrüßung über eine REST-Schnittstelle

Diese Aufgabe ergänzt die bestehende CLI um einen zweiten Inbound-Adapter. Die Fachlogik bleibt unverändert: Sowohl die CLI als auch ein REST-Controller verwenden den Inbound-Port `SayHello` und damit denselben `GreetingService`.

---

## 🎯 Ziel der Aufgabe

Nach der Umsetzung ist die Begrüßung per HTTP erreichbar:

```text
GET /greeting?name=Alice
```

Die Antwort ist einfacher Text (`text/plain`):

```text
Herzlich willkommen zum ersten Mal, Alice!
```

Fehlt der Queryparameter `name`, ist er leer oder besteht nur aus Leerzeichen, antwortet der Endpunkt bewusst ebenfalls mit **HTTP 200**:

```text
Wie heißt die zu grüßende Person?
```

Die vorhandene CLI bleibt im Code erhalten und kann weiterhin als alternativer Inbound-Adapter ausgeführt werden.

---

## 💡 Architektur-Lerneffekt

Die Eingabequelle ist austauschbar, die Anwendungsschicht bleibt dieselbe:

```text
CLI                                  REST
GreetingCliAdapter                   GreetingRestController
        │                                     │
        └─────────────── SayHello ────────────┘
                              │
                       GreetingService
                              │
                     UserLoginNotifier
```

Der Controller darf keine Begrüßung selbst zusammensetzen und keine Klassen aus `notification` kennen. Er validiert nur die HTTP-Eingabe, erstellt einen `User` und delegiert an `SayHello`.

Die Antwort ist eine Verantwortung des Inbound-Adapters. Deshalb liefert `SayHello` einen `String` zurück. Die CLI schreibt diesen String auf die Konsole, der REST-Controller liefert ihn als HTTP-Body zurück.

---

## 📋 Schritt-für-Schritt-Anleitung

### Schritt 1: Spring MVC ergänzen

Füge in der `pom.xml` die Web-MVC-Abhängigkeit hinzu:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
```

Für die MVC-Tests ergänze zusätzlich diese Test-Abhängigkeit:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
```

Der Spring-Boot-Parent verwaltet die Version. Der Starter bringt unter anderem einen eingebetteten Tomcat-Server mit.

---

### Schritt 2: Die Anwendung für den Webserver geöffnet lassen

Die bisherige CLI-Anwendung beendet den Spring-Kontext nach dem Start explizit. Für einen Webserver wäre das falsch: Der Server würde direkt nach dem erfolgreichen Start wieder beendet.

Passe die `main`-Methode deshalb auf folgenden Inhalt an:

```java
public static void main(String[] args) {
    SpringApplication.run(PortsAdapterApplication.class, args);
}
```

Entferne insbesondere diese Aufrufe aus der bisherigen `main`-Methode:

```java
SpringApplication.exit(context);
System.exit(exitCode);
```

---

### Schritt 3: Den Inbound-Port auf eine Rückgabe umstellen

`SayHello` muss die erzeugte Begrüßung zurückliefern:

```java
public interface SayHello {

    String sayHello(User user);
}
```

Passe `GreetingService` entsprechend an:

1. Erzeuge die Begrüßung wie bisher abhängig von der Besuchszahl.
2. Sende weiterhin die Login-Benachrichtigung über `UserLoginNotifier`.
3. Gib die erzeugte Begrüßung als `String` zurück.

Die Anwendungsschicht soll die Begrüßung nicht mehr selbst auf die Konsole schreiben. Entferne daher die Abhängigkeit auf `Printer` aus `GreetingService`.

---

### Schritt 4: Die CLI als Alternative erhalten

Passe `GreetingCliAdapter` an:

1. Rufe `sayHello.sayHello(new User(args[0]))` auf.
2. Schreibe den zurückgegebenen Text mit `IO.println(...)` auf die Konsole.
3. Behalte die bestehende Validierung für fehlende oder leere CLI-Argumente bei.

Der bisherige `Printer`-Outbound-Port und `SystemOutPrinter` werden danach nicht mehr benötigt und können entfernt werden.

Sobald Spring MVC vorhanden ist, startet die Anwendung standardmäßig als Web-Anwendung. Damit die CLI trotzdem bewusst als Alternative gestartet werden kann, aktiviere sie nur über eine Property:

```java
@Component
@ConditionalOnProperty(name = "app.cli.enabled", havingValue = "true")
public class GreetingCliAdapter implements CommandLineRunner {
    // ...
}
```

Da die Anwendung mit dem Web-MVC-Starter standardmäßig als Web-Anwendung startet, verwende für den reinen CLI-Modus zusätzlich `spring.main.web-application-type=none`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="Alice --app.cli.enabled=true --spring.main.web-application-type=none"
```

---

### Schritt 5: REST-Controller erstellen

Erstelle `GreetingRestController` im Package `greeting.adapter.in.rest`:

```java
package de.obscure1910.pattern.portsadapter.greeting.adapter.in.rest;

import de.obscure1910.pattern.portsadapter.greeting.application.port.in.SayHello;
import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class GreetingRestController {

    private final SayHello sayHello;

    GreetingRestController(SayHello sayHello) {
        this.sayHello = sayHello;
    }

    @GetMapping(value = "/greeting", produces = MediaType.TEXT_PLAIN_VALUE)
    String greeting(@RequestParam(required = false) String name) {
        if (name == null || name.isBlank()) {
            return "Wie heißt die zu grüßende Person?";
        }

        return sayHello.sayHello(new User(name));
    }
}
```

Wichtig: `@RequestParam(required = false)` verhindert, dass Spring bei einem fehlenden `name`-Parameter selbst eine HTTP-400-Antwort erzeugt. Der Controller kann dadurch die gewünschte HTTP-200-Antwort mit der Rückfrage liefern.

---

### Schritt 6: REST-Endpunkt ausprobieren

Starte die Anwendung ohne CLI-Property:

```bash
./mvnw spring-boot:run
```

Rufe den Endpunkt in einem zweiten Terminal auf:

```bash
curl "http://localhost:8888/greeting?name=Alice"
```

Erwartete Antwort:

```text
Herzlich willkommen zum ersten Mal, Alice!
```

Teste anschließend den fehlenden Namen:

```bash
curl -i "http://localhost:8888/greeting"
```

Erwartet werden HTTP 200 und:

```text
Wie heißt die zu grüßende Person?
```

---

## 🧪 Tests

### 1. `GreetingServiceTest` anpassen

- Prüfe den zurückgegebenen Begrüßungstext statt einer Interaktion mit `Printer`.
- Behalte die Prüfung bei, dass `UserLoginNotifier` die passende `UserLoginNotification` erhält.
- Verwende einen festen `Instant` im Test.

### 2. `GreetingCliAdapterTest` anpassen

- Passe das Mocking von `SayHello` an den zurückgegebenen `String` an.
- Behalte die Tests für fehlende und leere Namen bei.
- Stelle sicher, dass der Adapter den `User` weiterhin an `SayHello` delegiert.

### 3. `GreetingRestControllerTest` erstellen

Verwende einen auf den Controller begrenzten MVC-Test mit `@WebMvcTest`. Stelle `SayHello` über `@MockitoBean` bereit; `@WebMvcTest` konfiguriert `MockMvc` automatisch.

Prüfe mindestens diese Fälle:

| Request | Erwartung |
|---|---|
| `GET /greeting?name=Alice` | HTTP 200, `text/plain`, Begrüßung aus `SayHello` |
| `GET /greeting` | HTTP 200, `Wie heißt die zu grüßende Person?` |
| `GET /greeting?name=%20%20%20` | HTTP 200, `Wie heißt die zu grüßende Person?` |

### 4. Bestehende Architekturtests ausführen

Die Spring-Modulith- und ArchUnit-Tests müssen unverändert grün bleiben. Der REST-Controller darf nur auf den eigenen Inbound-Port `SayHello` und das eigene Domain Model `User` zugreifen.

Führe alle Tests aus:

```bash
./mvnw test
```

---

## 🏆 Definition of Done

- [ ] `spring-boot-starter-webmvc` ist eingebunden.
- [ ] `spring-boot-starter-webmvc-test` ist nur im Test-Scope eingebunden.
- [ ] Die `main`-Methode beendet den Spring-Kontext nach dem Start nicht mehr explizit.
- [ ] `SayHello` und `GreetingService` liefern die Begrüßung als `String` zurück.
- [ ] `GreetingService` verwendet keinen `Printer` mehr.
- [ ] Die CLI bleibt als optional aktivierbarer Inbound-Adapter erhalten.
- [ ] `GreetingRestController` beantwortet `GET /greeting?name=Alice` mit HTTP 200 und `text/plain`.
- [ ] Fehlende oder leere Namen liefern HTTP 200 mit `Wie heißt die zu grüßende Person?`.
- [ ] Der Controller delegiert über `SayHello` und enthält keine fachliche Begrüßungslogik.
- [ ] Die REST-Controller-Tests decken Erfolg, fehlenden und leeren Namen ab.
- [ ] Die bestehenden Spring-Modulith- und ArchUnit-Tests bleiben grün.
- [ ] `./mvnw test` ist grün.
