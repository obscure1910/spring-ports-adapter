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

Das Feature `greeting` meldet einen Login über einen eigenen Outbound-Port. Erst dessen Adapter kennt das Feature `notification`:

```text
GreetingService
    → UserLoginNotifier
    → NotificationModuleUserLoginNotifier
    → NotifyUserLogin
    → LoginNotificationService
    → LoginEventPublisher
    → ConsoleThirdSystemAdapter
```

Die fachliche Anwendung von `greeting` kennt keine Klassen aus `notification`:

```text
greeting.application
    → greeting.application.port.out.notification

greeting.adapter.out.notification
    → notification.application.port.in
```

`GreetingService` verwendet nur `UserLoginNotifier` und `UserLoginNotification` aus dem eigenen Feature. Ausschließlich `greeting.adapter.out.notification` darf `NotifyUserLogin` und `LoginNotificationCommand` aus `notification.application.port.in` verwenden. Weder der Adapter noch andere Greeting-Klassen dürfen `LoginNotificationService`, ein Domain Model oder einen Adapter aus `notification` kennen.

Diese Regel ist nicht nur eine Konvention: **Spring Modulith** schützt die Modulgrenze und **ArchUnit** begrenzt den Zugriff innerhalb von `greeting` auf den einen Outbound-Adapter.

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

Zusätzlich wird das vorhandene Feature `greeting` um zwei Outbound-Ports erweitert:

```text
greeting
├── application
│   └── port
│       └── out
│           ├── notification
│           │   ├── UserLoginNotification.java
│           │   └── UserLoginNotifier.java
│           └── time
│               └── CurrentTimeProvider.java
└── adapter
    └── out
        ├── notification
        │   └── NotificationModuleUserLoginNotifier.java
        └── time
            └── SystemCurrentTimeAdapter.java
```

---

## 📋 Schritt-für-Schritt-Anleitung

### Schritt 1: Öffentlicher Übergabevertrag und Inbound-Port

Der Adapter aus `greeting` darf nicht das Domain Model von `notification` erstellen. Er übergibt deshalb ein Command über den öffentlichen Inbound-Port.

Erstelle dieses Übergabeobjekt im Package `notification.application.port.in`:

```java
package de.obscure1910.pattern.portsadapter.notification.application.port.in;

import java.time.Instant;

public record LoginNotificationCommand(String username, Instant loggedInAT) {
}
```

Erstelle anschließend den Inbound-Port:

```java
package de.obscure1910.pattern.portsadapter.notification.application.port.in;

public interface NotifyUserLogin {

    void notify(LoginNotificationCommand command);
}
```

`LoginNotificationCommand` ist Teil des öffentlichen Vertrags: Es beschreibt die Daten, die der Notification-Outbound-Adapter aus `greeting` an `notification` übergibt.

---

### Schritt 2: Domain Model im Feature `notification`

Erstelle das fachliche Domain Model im Package `notification.domain.model`:

```java
package de.obscure1910.pattern.portsadapter.notification.domain.model;

import java.time.Instant;

public record UserLogin(String username, Instant loggedInAt) {
}
```

`UserLogin` ist ein internes Domain Model von `notification`. Es darf nur innerhalb dieses Features verwendet werden; `greeting` importiert diese Klasse nicht.

---

### Schritt 3: Outbound-Port für das Drittsystem

Erstelle im Package `notification.application.port.out` dieses Interface:

```java
package de.obscure1910.pattern.portsadapter.notification.application.port.out;

import de.obscure1910.pattern.portsadapter.notification.domain.model.UserLogin;

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
        UserLogin login = new UserLogin(command.username(), command.loggedInAT());
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

Die Ausgabe mit `IO.println(...)` erfolgt ausschließlich in diesem Adapter, nicht im `LoginNotificationService`.

---

### Schritt 6: Port für die aktuelle Zeit im Feature `greeting`

Damit der Zeitpunkt im Test kontrollierbar bleibt, darf `GreetingService` nicht direkt `Instant.now()` aufrufen.

Erstelle deshalb einen Outbound-Port:

```java
package de.obscure1910.pattern.portsadapter.greeting.application.port.out.time;

import java.time.Instant;

public interface CurrentTimeProvider {

    Instant now();
}
```

Erstelle dazu `SystemCurrentTimeAdapter` im Package `greeting.adapter.out.time`. Er implementiert den Port, erhält per Constructor Injection eine `Clock` und gibt `Instant.now(clock)` zurück. Die Anwendung stellt dafür über eine `TimeConfiguration` eine `Clock.systemUTC()` als Spring Bean bereit.

---

### Schritt 7: Eigenen Notification-Outbound-Port in `greeting` anlegen

Erstelle im Package `greeting.application.port.out.notification` die beiden Typen:

```java
public record UserLoginNotification(String username, Instant loggedInAt) {
}
```

```java
public interface UserLoginNotifier {

    void notify(UserLoginNotification notification);
}
```

Erweitere den Konstruktor von `GreetingService` um:

```java
CurrentTimeProvider currentTimeProvider
UserLoginNotifier userLoginNotifier
```

In `sayHello(User user)`:

1. Erfasse den Zeitpunkt über `currentTimeProvider.now()`.
2. Führe die bestehende Begrüßungslogik mit Besuchszähler aus.
3. Erstelle `new UserLoginNotification(user.name(), currentTime)`.
4. Rufe `userLoginNotifier.notify(...)` auf.

`GreetingService` importiert dabei keine Klassen aus `notification`.

### Schritt 8: Notification-Outbound-Adapter in `greeting` anlegen

Erstelle `NotificationModuleUserLoginNotifier` im Package `greeting.adapter.out.notification`. Der Adapter implementiert `UserLoginNotifier`, erhält `NotifyUserLogin` per Constructor Injection und übersetzt das eigene `UserLoginNotification` in ein `LoginNotificationCommand`:

```java
@Component
public class NotificationModuleUserLoginNotifier implements UserLoginNotifier {

    private final NotifyUserLogin notifyUserLogin;

    public NotificationModuleUserLoginNotifier(NotifyUserLogin notifyUserLogin) {
        this.notifyUserLogin = notifyUserLogin;
    }

    @Override
    public void notify(UserLoginNotification notification) {
        notifyUserLogin.notify(
                new LoginNotificationCommand(notification.username(), notification.loggedInAt())
        );
    }
}
```

Nur dieser Adapter darf Klassen aus `notification.application.port.in` importieren.

---

### Schritt 9: Öffentliche Modul-Schnittstelle deklarieren

Spring Modulith behandelt die Packages `greeting` und `notification` als eigenständige Application Modules. Markiere deshalb den Inbound-Port von `notification` als die einzige öffentliche Schnittstelle für andere Features.

Erstelle diese Datei:

```text
src/main/java/de/obscure1910/pattern/portsadapter/notification/application/port/in/package-info.java
```

```java
@org.springframework.modulith.NamedInterface("in")
package de.obscure1910.pattern.portsadapter.notification.application.port.in;
```

Damit dürfen andere Module gezielt auf `NotifyUserLogin` und `LoginNotificationCommand` zugreifen, aber nicht auf interne Klassen von `notification` wie das Domain Model `UserLogin`.

---

### Schritt 10: Erlaubte Modul-Abhängigkeit festlegen

Erstelle diese Datei:

```text
src/main/java/de/obscure1910/pattern/portsadapter/greeting/package-info.java
```

```java
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = "notification :: in"
)
package de.obscure1910.pattern.portsadapter.greeting;
```

Damit ist ausschließlich diese feature-übergreifende Richtung aus `greeting` erlaubt:

```text
greeting → notification :: in
```

---

### Schritt 11: Zugriff auf `notification` mit ArchUnit auf den Outbound-Adapter begrenzen

Spring Modulith prüft, dass `greeting` ausschließlich die benannte Schnittstelle `notification :: in` verwendet. Ergänze zusätzlich einen ArchUnit-Test, der zwei feinere Regeln absichert:

1. Nur `greeting.adapter.out.notification` darf Klassen aus dem Modul `notification` verwenden.
2. Dieser Adapter darf aus `notification` ausschließlich Klassen aus `notification.application.port.in` verwenden.

Füge zuerst diese Test-Abhängigkeit in die `pom.xml` ein:

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.4.2</version>
    <scope>test</scope>
</dependency>
```

Erstelle anschließend zum Beispiel diese Testklasse:

```java
package de.obscure1910.pattern.portsadapter.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(
        packages = "de.obscure1910.pattern.portsadapter",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class PortsAdapterArchitectureTest {

    @ArchTest
    static final ArchRule greetingMayDependOnNotificationOnlyThroughNotificationOutboundAdapter =
            classes()
                    .that().resideInAPackage("de.obscure1910.pattern.portsadapter.greeting..")
                    .and().resideOutsideOfPackage("de.obscure1910.pattern.portsadapter.greeting.adapter.out.notification..")
                    .should().onlyDependOnClassesThat(
                            resideOutsideOfPackage("de.obscure1910.pattern.portsadapter.notification..")
                    );

    @ArchTest
    static final ArchRule notificationOutboundAdapterMayDependOnNotificationOnlyThroughInboundPort =
            classes()
                    .that().resideInAPackage("de.obscure1910.pattern.portsadapter.greeting.adapter.out.notification..")
                    .should().onlyDependOnClassesThat(
                            resideOutsideOfPackage("de.obscure1910.pattern.portsadapter.notification..")
                                    .or(resideInAPackage("de.obscure1910.pattern.portsadapter.notification.application.port.in.."))
                    );
}
```

Die Paketnamen sind absichtlich vollständig qualifiziert: Ein allgemeines Muster wie `..notification..` würde auch das Paket `greeting.application.port.out.notification` erfassen und dadurch fälschlich fehlschlagen.

Die Regeln umfassen Imports, Feldtypen, Parameter, Rückgabetypen und Methodenaufrufe. Ein direkter Import von `NotifyUserLogin` in `GreetingService` oder ein Import von `LoginNotificationService` im Outbound-Adapter lässt den jeweiligen Test fehlschlagen.

---

## 🧪 Tests

### 1. `GreetingServiceTest`

- Mocke `CurrentTimeProvider`, `UserLoginNotifier`, `GreetingHistoryRepository` und `Printer`.
- Lasse `CurrentTimeProvider.now()` einen festen `Instant` zurückgeben.
- Rufe `greetingService.sayHello(new User("Alice"))` auf.
- Prüfe, dass `UserLoginNotifier` genau einmal mit `new UserLoginNotification("Alice", fixedInstant)` aufgerufen wurde.
- Stelle weiterhin sicher, dass die passende Begrüßung ausgegeben wird.

### 2. `NotificationModuleUserLoginNotifierTest`

- Mocke `NotifyUserLogin`.
- Übergib ein `UserLoginNotification` an `NotificationModuleUserLoginNotifier.notify(...)`.
- Prüfe, dass `NotifyUserLogin` genau einmal mit dem entsprechend übersetzten `LoginNotificationCommand` aufgerufen wird.

### 3. `LoginNotificationServiceTest`

- Mocke `LoginEventPublisher`.
- Übergib ein `LoginNotificationCommand` an `LoginNotificationService.notify(...)`.
- Prüfe, dass `publisher.publish(...)` ein fachlich gleiches `new UserLogin("Alice", fixedInstant)` erhält.

### 4. Spring-Kontext

- Stelle sicher, dass der Spring-Kontext genau eine Implementierung für jeden benötigten Port findet.

### 5. Modultest mit Spring Modulith

Erstelle einen Test, zum Beispiel `ModularityTest`, im Root-Package der Tests:

```java
package de.obscure1910.pattern.portsadapter;

import de.obscure1910.pattern.portsadapter.PortsAdapterApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {

    @Test
    void verifiesModuleStructure() {
        ApplicationModules.of(PortsAdapterApplication.class).verify();
    }
}
```

Der Test muss fehlschlagen, wenn beispielsweise `GreetingService` direkt `LoginNotificationService` oder `ConsoleThirdSystemAdapter` importiert. Der Zugriff auf `NotifyUserLogin` wäre auf Modul-Ebene zulässig, weil es zur Named Interface `notification :: in` gehört. Die ArchUnit-Regeln begrenzen diesen Zugriff zusätzlich auf `greeting.adapter.out.notification`.

### 6. Architekturtest mit ArchUnit

Der in Schritt 11 erstellte `PortsAdapterArchitectureTest` muss ebenfalls Bestandteil des normalen Maven-Testlaufs sein. Er verhindert insbesondere:

```text
greeting außerhalb von adapter.out.notification → notification

greeting.adapter.out.notification → notification außerhalb von application.port.in
```

### 7. Alle Tests ausführen

Führe anschließend alle Tests aus:

```bash
./mvnw test
```

---

## 🏆 Definition of Done

- [ ] `notification` besitzt einen Inbound-Port, einen Application Service, einen Outbound-Port und einen Outbound-Adapter.
- [ ] `GreetingService` übergibt Benutzername und Login-Zeitpunkt ausschließlich als `UserLoginNotification` an seinen eigenen Outbound-Port.
- [ ] `NotificationModuleUserLoginNotifier` übersetzt `UserLoginNotification` in `LoginNotificationCommand` und delegiert an `NotifyUserLogin`.
- [ ] `LoginNotificationService` erzeugt daraus das interne Domain Model `UserLogin`.
- [ ] Das Drittsystem erhält Benutzername und Login-Zeitpunkt als `UserLogin`.
- [ ] `GreetingService` kennt keine Klassen aus `notification`.
- [ ] Nur `greeting.adapter.out.notification` kennt Klassen aus `notification.application.port.in`.
- [ ] `Instant.now(clock)` wird nur im `SystemCurrentTimeAdapter` aufgerufen.
- [ ] Alle Application Services nutzen Constructor Injection.
- [ ] `notification.application.port.in` ist in Spring Modulith als benannte Schnittstelle `in` markiert.
- [ ] `greeting` darf nur von `notification :: in` abhängen.
- [ ] `ApplicationModules.of(PortsAdapterApplication.class).verify()` prüft diese Regeln im Testlauf.
- [ ] ArchUnit beschränkt Notification-Abhängigkeiten auf `greeting.adapter.out.notification` und `notification.application.port.in`.
- [ ] Die Tests für beide Features laufen isoliert und `./mvnw test` ist grün.
