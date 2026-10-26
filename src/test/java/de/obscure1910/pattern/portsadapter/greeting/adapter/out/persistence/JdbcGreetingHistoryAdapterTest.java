package de.obscure1910.pattern.portsadapter.greeting.adapter.out.persistence;

import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcGreetingHistoryAdapterTest {

    @Test
    void incrementsVisitsForTheSameUser() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:greeting-history;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE greeting_history (user_name VARCHAR(255) PRIMARY KEY, visit_count INTEGER NOT NULL)");
        JdbcGreetingHistoryAdapter adapter = new JdbcGreetingHistoryAdapter(jdbcTemplate);

        assertThat(adapter.incrementAndGetVisits(new User("Alice"))).isEqualTo(1);
        assertThat(adapter.incrementAndGetVisits(new User("Alice"))).isEqualTo(2);
    }
}
