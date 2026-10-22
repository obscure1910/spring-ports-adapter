package de.obscure1910.pattern.ports.portsadapter.greeting.application.port.out.persistence;

import de.obscure1910.pattern.ports.portsadapter.greeting.domain.model.User;

public interface GreetingHistoryRepository {

    int incrementAndGetVisits(User user);

}
