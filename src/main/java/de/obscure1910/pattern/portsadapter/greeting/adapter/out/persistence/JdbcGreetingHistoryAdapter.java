package de.obscure1910.pattern.portsadapter.greeting.adapter.out.persistence;

import de.obscure1910.pattern.portsadapter.greeting.application.port.out.persistence.GreetingHistoryRepository;
import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!in-memory")
public class JdbcGreetingHistoryAdapter implements GreetingHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcGreetingHistoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public synchronized int incrementAndGetVisits(User user) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE greeting_history SET visit_count = visit_count + 1 WHERE user_name = ?", user.name());
        if (updatedRows == 0) {
            try {
                jdbcTemplate.update("INSERT INTO greeting_history (user_name, visit_count) VALUES (?, 1)", user.name());
            } catch (DuplicateKeyException exception) {
                jdbcTemplate.update(
                        "UPDATE greeting_history SET visit_count = visit_count + 1 WHERE user_name = ?", user.name());
            }
        }
        Integer visits = jdbcTemplate.queryForObject(
                "SELECT visit_count FROM greeting_history WHERE user_name = ?", Integer.class, user.name());
        return visits;
    }
}
