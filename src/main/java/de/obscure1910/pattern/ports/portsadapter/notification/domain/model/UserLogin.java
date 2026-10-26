package de.obscure1910.pattern.ports.portsadapter.notification.domain.model;

import java.time.Instant;

public record UserLogin(String username, Instant loggedInAt) {
}
