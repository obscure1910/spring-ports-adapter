package de.obscure1910.pattern.portsadapter.greeting.adapter.out.persistence;

import de.obscure1910.pattern.portsadapter.greeting.application.port.out.persistence.GreetingHistoryRepository;
import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("in-memory")
public class InMemoryGreetingHistoryAdapter implements GreetingHistoryRepository {

    private final Map<User, Integer> visits = new ConcurrentHashMap<>();

    @Override
    public int incrementAndGetVisits(User user) {
        return visits.merge(user, 1, Integer::sum);
    }

}
