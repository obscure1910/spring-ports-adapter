package de.obscure1910.pattern.portsadapter.greeting.application.port.out.notification;

import java.time.Instant;

public record UserLoginNotification(String username, Instant loggedInAt) {
}
