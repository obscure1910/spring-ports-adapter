package de.obscure1910.pattern.portsadapter.greeting.adapter.out.persistence;

import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
    void bindsUserNamesContainingSqlSyntaxAsData() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:parameter-binding;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE greeting_history (user_name VARCHAR(255) PRIMARY KEY, visit_count INTEGER NOT NULL)");
        JdbcGreetingHistoryAdapter adapter = new JdbcGreetingHistoryAdapter(jdbcTemplate);
        User user = new User("Ada'); DROP TABLE greeting_history; --");

        assertThat(adapter.incrementAndGetVisits(user)).isEqualTo(1);
        assertThat(adapter.incrementAndGetVisits(user)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM greeting_history", Integer.class)).isEqualTo(1);
    }

    @Test
    void retainsEveryConcurrentVisitForTheSameUser() throws InterruptedException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:concurrent-greeting-history;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE greeting_history (user_name VARCHAR(255) PRIMARY KEY, visit_count INTEGER NOT NULL)");
        JdbcGreetingHistoryAdapter adapter = new JdbcGreetingHistoryAdapter(jdbcTemplate);
        TransactionTemplate transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        int visits = 30;
        var executor = Executors.newFixedThreadPool(16);

        java.util.List<Future<Integer>> visitAllocations = new java.util.ArrayList<>();
        for (int index = 0; index < visits; index++) {
            visitAllocations.add(executor.submit(() -> transactionTemplate.execute(
                    status -> adapter.incrementAndGetVisits(new User("Alice")))));
        }
        executor.shutdown();

        assertThat(executor.awaitTermination(30, TimeUnit.SECONDS)).isTrue();
        assertThat(visitAllocations.stream().map(this::get).toList())
                .containsExactlyInAnyOrderElementsOf(java.util.stream.IntStream.rangeClosed(1, visits).boxed().toList());
        assertThat(jdbcTemplate.queryForObject(
                "SELECT visit_count FROM greeting_history WHERE user_name = ?", Integer.class, "Alice"))
                .isEqualTo(visits);
    }

    private Integer get(Future<Integer> allocation) {
        try {
            return allocation.get();
        } catch (Exception exception) {
            throw new AssertionError("Concurrent visit allocation failed", exception);
        }
    }

    private JdbcGreetingHistoryAdapter adapterFor(String url) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(url);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS greeting_history (user_name VARCHAR(255) PRIMARY KEY, visit_count INTEGER NOT NULL)");
        return new JdbcGreetingHistoryAdapter(jdbcTemplate);
    }
}
