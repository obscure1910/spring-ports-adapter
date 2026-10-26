package de.obscure1910.pattern.portsadapter.greeting.adapter.out.persistence;

import de.obscure1910.pattern.portsadapter.greeting.application.port.out.persistence.GreetingHistoryRepository;
import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!in-memory")
public class JdbcGreetingHistoryAdapter implements GreetingHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcGreetingHistoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public int incrementAndGetVisits(User user) {
        jdbcTemplate.update("""
                MERGE INTO greeting_history (user_name, visit_count)
                KEY (user_name)
                VALUES (?, COALESCE((SELECT visit_count + 1 FROM greeting_history WHERE user_name = ?), 1))
                """, user.name(), user.name());
        Integer visits = jdbcTemplate.queryForObject(
                "SELECT visit_count FROM greeting_history WHERE user_name = ?", Integer.class, user.name());
        return visits;
    }
}
