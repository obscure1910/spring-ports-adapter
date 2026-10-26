package de.obscure1910.pattern.portsadapter.greeting.application.port.out.persistence;

import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;

public interface GreetingHistoryRepository {

    int incrementAndGetVisits(User user);

}
