package de.obscure1910.pattern.portsadapter.greeting.adapter.in.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:greeting-http;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=always"
})
class GreetingConcurrencyIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void retainsAndAllocatesEveryVisitAcrossThirtyConcurrentHttpRequests() {
        String userName = "Concurrent-" + UUID.randomUUID();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(greetingUri(userName)).GET().build();

        List<String> greetings = IntStream.range(0, 30)
                .mapToObj(ignored -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body))
                .map(CompletableFuture::join)
                .toList();

        String firstGreeting = "Herzlich willkommen zum ersten Mal, " + userName + "!";
        assertThat(greetings.stream()
                .filter(firstGreeting::equals)
                .count()).isEqualTo(1);
        Set<String> expectedGreetings = IntStream.rangeClosed(2, 30)
                .mapToObj(visit -> "Schön dich wiederzusehen, " + userName + "! (Besuch #" + visit + ")")
                .collect(java.util.stream.Collectors.toSet());
        assertThat(greetings).containsExactlyInAnyOrderElementsOf(
                java.util.stream.Stream.concat(java.util.stream.Stream.of(firstGreeting), expectedGreetings.stream()).toList());
        assertThat(jdbcTemplate.queryForObject(
                "SELECT visit_count FROM greeting_history WHERE user_name = ?", Integer.class, userName))
                .isEqualTo(30);
    }

    private URI greetingUri(String userName) {
        return URI.create("http://localhost:" + port + "/greeting?name="
                + URLEncoder.encode(userName, StandardCharsets.UTF_8));
    }
}
