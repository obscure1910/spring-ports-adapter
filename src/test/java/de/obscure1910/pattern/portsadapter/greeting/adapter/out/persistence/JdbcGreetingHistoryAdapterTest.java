package de.obscure1910.pattern.portsadapter.greeting.adapter.out.persistence;

import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    @Test
    void persistsVisitsAcrossDataSourceLifecycles(@org.junit.jupiter.api.io.TempDir Path temporaryDirectory) {
        String url = "jdbc:h2:file:" + temporaryDirectory.resolve("greeting-history") + ";DB_CLOSE_ON_EXIT=FALSE";
        JdbcGreetingHistoryAdapter firstAdapter = adapterFor(url);

        assertThat(firstAdapter.incrementAndGetVisits(new User("Ada Lovelace's Test"))).isEqualTo(1);

        JdbcGreetingHistoryAdapter restartedAdapter = adapterFor(url);

        assertThat(restartedAdapter.incrementAndGetVisits(new User("Ada Lovelace's Test"))).isEqualTo(2);
    }

    @Test
    void retainsEveryConcurrentVisitForTheSameUser() throws InterruptedException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:concurrent-greeting-history;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE greeting_history (user_name VARCHAR(255) PRIMARY KEY, visit_count INTEGER NOT NULL)");
        JdbcGreetingHistoryAdapter adapter = new JdbcGreetingHistoryAdapter(jdbcTemplate);
        int visits = 200;
        var executor = Executors.newFixedThreadPool(16);

        for (int index = 0; index < visits; index++) {
            executor.submit(() -> adapter.incrementAndGetVisits(new User("Alice")));
        }
        executor.shutdown();

        assertThat(executor.awaitTermination(30, TimeUnit.SECONDS)).isTrue();
        assertThat(adapter.incrementAndGetVisits(new User("Alice"))).isEqualTo(visits + 1);
    }

    private JdbcGreetingHistoryAdapter adapterFor(String url) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(url);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS greeting_history (user_name VARCHAR(255) PRIMARY KEY, visit_count INTEGER NOT NULL)");
        return new JdbcGreetingHistoryAdapter(jdbcTemplate);
    }
}
