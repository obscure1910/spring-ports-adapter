package de.obscure1910.pattern.portsadapter.notification.application.port.in;

import java.time.Instant;

public record LoginNotificationCommand(String username, Instant loggedInAT) {
}
