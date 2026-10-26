package de.obscure1910.pattern.portsadapter.notification.domain.model;

import java.time.Instant;

public record UserLogin(String username, Instant loggedInAt) {
}
