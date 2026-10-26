# Übungsaufgabe 2: Login-Benachrichtigung an ein Drittsystem

Diese Aufgabe erweitert die Anwendung um ein zweites Feature: Bei einer Begrüßung wird der Login eines Benutzers inklusive Benutzername und Zeitpunkt an ein Drittsystem gemeldet.

Das Drittsystem ist zunächst bewusst einfach gehalten und wird durch eine Konsolenausgabe simuliert. Später könnte derselbe Port durch einen HTTP-, Kafka- oder E-Mail-Adapter implementiert werden.

---

## 🎯 Ziel der Aufgabe

Der bestehende Aufruf bleibt unverändert:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="Alice"
```

Neben der bekannten Begrüßung soll die Anwendung eine Meldung für das Drittsystem ausgeben:

```text
Herzlich willkommen zum ersten Mal, Alice!
Drittsystem informiert: user=Alice, loggedInAt=2026-10-22T16:52:46Z
```

Der Zeitstempel muss als `Instant` erfasst und zusammen mit dem Benutzernamen an das neue Feature übergeben werden.

---

## 💡 Architektur-Lerneffekt

Das vorhandene Feature `greeting` benötigt das neue Feature `notification`:

```text
GreetingService
    → NotifyUserLogin
    → LoginNotificationService
    → LoginEventPublisher
    → ConsoleThirdSystemAdapter
```

Die Abhängigkeit zeigt nur in **eine** Richtung:

```text
greeting → notification
```

`greeting` kennt nur den öffentlichen Inbound-Port und das Übergabeobjekt aus `notification`. Es darf weder `LoginNotificationService` noch einen Adapter des neuen Features kennen. Umgekehrt kennt `notification` nichts aus dem `greeting`-Feature.

Diese Regel ist nicht nur eine Konvention: Der öffentliche Modulvertrag wird mit **Spring Modulith** und die explizite Verbotsrichtung mit **ArchUnit** automatisiert geprüft. Die Abhängigkeiten für Spring Modulith sind bereits im Projekt vorhanden.

---

## 📦 Vorgeschlagene Package-Struktur

```text
notification
├── domain
│   └── model
│       └── UserLogin.java
├── application
│   ├── LoginNotificationService.java
│   └── port
│       ├── in
│       │   ├── NotifyUserLogin.java
│       │   └── LoginNotificationCommand.java
│       └── out
│           └── LoginEventPublisher.java
└── adapter
    └── out
        └── console
            └── ConsoleThirdSystemAdapter.java
```

Zusätzlich wird das vorhandene Feature `greeting` um einen Port für die aktuelle Zeit erweitert:

```text
greeting
├── application
│   └── port
│       └── out
│           └── time
│               └── CurrentTimeProvider.java
└── adapter
    └── out
        └── time
            └── SystemCurrentTimeAdapter.java
```

---

## 📋 Schritt-für-Schritt-Anleitung

### Schritt 1: Öffentlicher Übergabevertrag und Inbound-Port

Der Aufrufer aus `greeting` darf nicht das Domain Model von `notification` erstellen. Er übergibt deshalb ein Command über den öffentlichen Inbound-Port.

Erstelle dieses Übergabeobjekt im Package `notification.application.port.in`:

```java
package de.obscure1910.pattern.ports.portsadapter.notification.application.port.in;

import java.time.Instant;

public record LoginNotificationCommand(String username, Instant loggedInAt) {
}
```

Erstelle anschließend den Inbound-Port:

```java
package de.obscure1910.pattern.ports.portsadapter.notification.application.port.in;

public interface NotifyUserLogin {

    void notify(LoginNotificationCommand command);
}
```

`LoginNotificationCommand` ist Teil des öffentlichen Vertrags: Es beschreibt die Daten, die `greeting` an `notification` übergibt.

---

### Schritt 2: Domain Model im Feature `notification`

Erstelle das fachliche Domain Model im Package `notification.domain.model`:

```java
package de.obscure1910.pattern.ports.portsadapter.notification.domain.model;

import java.time.Instant;

public record UserLogin(String username, Instant loggedInAt) {
}
```

`UserLogin` ist ein internes Domain Model von `notification`. Es darf nur innerhalb dieses Features verwendet werden; `greeting` importiert diese Klasse nicht.

---

### Schritt 3: Outbound-Port für das Drittsystem

Erstelle im Package `notification.application.port.out` dieses Interface:

```java
package de.obscure1910.pattern.ports.portsadapter.notification.application.port.out;

import de.obscure1910.pattern.ports.portsadapter.notification.domain.model.UserLogin;

public interface LoginEventPublisher {

    void publish(UserLogin login);
}
```

Der Port beschreibt nur die fachlich benötigte Aktion: Ein Login-Ereignis wird veröffentlicht. Er enthält keine Details darüber, ob dies über HTTP, Kafka, E-Mail oder die Konsole geschieht.

---

### Schritt 4: Application Service für `notification`

Erstelle `LoginNotificationService` im Package `notification.application`.

Der Service soll:

1. `NotifyUserLogin` implementieren,
2. `LoginEventPublisher` per Constructor Injection erhalten,
3. aus dem erhaltenen `LoginNotificationCommand` ein `UserLogin` erzeugen,
4. dieses Domain Model an `publisher.publish(login)` weiterreichen.

Beispiel:

```java
@Service
public class LoginNotificationService implements NotifyUserLogin {

    private final LoginEventPublisher publisher;

    public LoginNotificationService(LoginEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void notify(LoginNotificationCommand command) {
        UserLogin login = new UserLogin(command.username(), command.loggedInAt());
        publisher.publish(login);
    }
}
```

---

### Schritt 5: Konsolenadapter als simuliertes Drittsystem

Erstelle `ConsoleThirdSystemAdapter` im Package `notification.adapter.out.console`.

Der Adapter implementiert `LoginEventPublisher` und gibt etwa folgenden Text aus:

```text
Drittsystem informiert: user=Alice, loggedInAt=2026-10-22T16:52:46Z
```

Der Aufruf von `System.out.println(...)` ist ausschließlich in diesem Adapter erlaubt, nicht im `LoginNotificationService`.

---

### Schritt 6: Port für die aktuelle Zeit im Feature `greeting`

Damit der Zeitpunkt im Test kontrollierbar bleibt, darf `GreetingService` nicht direkt `Instant.now()` aufrufen.

Erstelle deshalb einen Outbound-Port:

```java
package de.obscure1910.pattern.ports.portsadapter.greeting.application.port.out.time;

import java.time.Instant;

public interface CurrentTimeProvider {

    Instant now();
}
```

Erstelle dazu `SystemCurrentTimeAdapter` im Package `greeting.adapter.out.time`. Er implementiert den Port und gibt `Instant.now()` zurück.

---

### Schritt 7: `GreetingService` erweitern

Erweitere den Konstruktor von `GreetingService` um:

```java
CurrentTimeProvider timeProvider
NotifyUserLogin loginNotifier
```

In `sayHello(User user)`:

1. Erfasse den Zeitpunkt über `timeProvider.now()`.
2. Führe die bestehende Begrüßungslogik mit Besuchszähler aus.
3. Erstelle `new LoginNotificationCommand(user.name(), loginTime)`.
4. Rufe `loginNotifier.notify(...)` auf.

`GreetingService` darf ausschließlich diese Typen aus dem neuen Feature importieren:

```text
notification.application.port.in.NotifyUserLogin
notification.application.port.in.LoginNotificationCommand
```

---

### Schritt 8: Öffentliche Modul-Schnittstelle deklarieren

Spring Modulith behandelt die Packages `greeting` und `notification` als eigenständige Application Modules. Markiere deshalb den Inbound-Port von `notification` als die einzige öffentliche Schnittstelle für andere Features.

Erstelle diese Datei:

```text
src/main/java/de/obscure1910/pattern/ports/portsadapter/notification/application/port/in/package-info.java
```

```java
@org.springframework.modulith.NamedInterface("login-api")
package de.obscure1910.pattern.ports.portsadapter.notification.application.port.in;
```

Damit dürfen andere Module gezielt auf `NotifyUserLogin` und `LoginNotificationCommand` zugreifen, aber nicht auf interne Klassen von `notification` wie das Domain Model `UserLogin`.

---

### Schritt 9: Erlaubte Modul-Abhängigkeit festlegen

Erstelle diese Datei:

```text
src/main/java/de/obscure1910/pattern/ports/portsadapter/greeting/package-info.java
```

```java
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = "notification :: login-api"
)
package de.obscure1910.pattern.ports.portsadapter.greeting;
```

Damit ist ausschließlich diese feature-übergreifende Richtung aus `greeting` erlaubt:

```text
greeting → notification :: login-api
```

---

### Schritt 10: Verbotene Gegenrichtung mit ArchUnit absichern

Spring Modulith prüft, dass `greeting` ausschließlich die benannte Schnittstelle `notification :: login-api` verwendet. Ergänze zusätzlich einen ArchUnit-Test, damit ein Import aus `notification` nach `greeting` eindeutig fehlschlägt.

Füge zuerst diese Test-Abhängigkeit in die `pom.xml` ein:

```text
com.tngtech.archunit:archunit-junit5:1.5.1
```

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.5.1</version>
    <scope>test</scope>
</dependency>
```

Erstelle anschließend zum Beispiel diese Testklasse:

```java
package de.obscure1910.pattern.ports.portsadapter;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "de.obscure1910.pattern.ports.portsadapter")
public class ArchitectureTest {

    @ArchTest
    public static final ArchRule notificationMustNotDependOnGreeting = noClasses()
            .that().resideInAPackage("..notification..")
            .should().dependOnClassesThat().resideInAPackage("..greeting..");
}
```

Die Regel umfasst Imports, Feldtypen, Parameter, Rückgabetypen und Methodenaufrufe. Beispielsweise würde ein Import von `GreetingService` oder `GreetingHistoryRepository` in `notification` den Test fehlschlagen lassen.

---

## 🧪 Tests

### 1. `GreetingServiceTest`

- Mocke `CurrentTimeProvider`, `NotifyUserLogin`, `GreetingHistoryRepository` und `Printer`.
- Lasse `CurrentTimeProvider.now()` einen festen `Instant` zurückgeben.
- Rufe `greetingService.sayHello(new User("Alice"))` auf.
- Prüfe, dass `NotifyUserLogin` genau einmal mit `new LoginNotificationCommand("Alice", fixedInstant)` aufgerufen wurde.
- Stelle weiterhin sicher, dass die passende Begrüßung ausgegeben wird.

### 2. `LoginNotificationServiceTest`

- Mocke `LoginEventPublisher`.
- Übergib ein `LoginNotificationCommand` an `LoginNotificationService.notify(...)`.
- Prüfe, dass `publisher.publish(...)` ein fachlich gleiches `new UserLogin("Alice", fixedInstant)` erhält.

### 3. Spring-Kontext

- Stelle sicher, dass der Spring-Kontext genau eine Implementierung für jeden benötigten Port findet.

### 4. Modultest mit Spring Modulith

Erstelle einen Test, zum Beispiel `ModularityTest`, im Root-Package der Tests:

```java
package de.obscure1910.pattern.ports.portsadapter;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {

    @Test
    void verifiesModuleStructure() {
        ApplicationModules.of(PortsAdapterApplication.class).verify();
    }
}
```

Der Test muss fehlschlagen, wenn beispielsweise `GreetingService` direkt `LoginNotificationService` oder `ConsoleThirdSystemAdapter` importiert. Zulässig bleiben nur die Klassen in `notification.application.port.in`.

### 5. Architekturtest mit ArchUnit

Der in Schritt 10 erstellte `ArchitectureTest` muss ebenfalls Bestandteil des normalen Maven-Testlaufs sein. Er verhindert insbesondere die verbotene Richtung:

```text
notification → greeting
```

### 6. Alle Tests ausführen

Führe anschließend alle Tests aus:

```bash
./mvnw test
```

---

## 🏆 Definition of Done

- [ ] `notification` besitzt einen Inbound-Port, einen Application Service, einen Outbound-Port und einen Outbound-Adapter.
- [ ] `GreetingService` übergibt Benutzername und Login-Zeitpunkt ausschließlich als `LoginNotificationCommand`.
- [ ] `LoginNotificationService` erzeugt daraus das interne Domain Model `UserLogin`.
- [ ] Das Drittsystem erhält Benutzername und Login-Zeitpunkt als `UserLogin`.
- [ ] `GreetingService` ruft ausschließlich `NotifyUserLogin` auf, nicht `LoginNotificationService` oder einen konkreten Adapter.
- [ ] `LoginNotificationService` kennt keine Klassen aus dem Feature `greeting`.
- [ ] `Instant.now()` wird nur im `SystemCurrentTimeAdapter` aufgerufen.
- [ ] Alle Application Services nutzen Constructor Injection.
- [ ] `notification.application.port.in` ist in Spring Modulith als benannte Schnittstelle `login-api` markiert.
- [ ] `greeting` darf nur von `notification :: login-api` abhängen.
- [ ] `ApplicationModules.of(PortsAdapterApplication.class).verify()` prüft diese Regeln im Testlauf.
- [ ] Ein ArchUnit-Test verbietet jede Abhängigkeit von `notification` nach `greeting`.
- [ ] Die Tests für beide Features laufen isoliert und `./mvnw test` ist grün.
