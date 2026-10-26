package de.obscure1910.pattern.portsadapter;

import de.obscure1910.pattern.portsadapter.greeting.adapter.out.persistence.JdbcGreetingHistoryAdapter;
import de.obscure1910.pattern.portsadapter.greeting.application.port.out.persistence.GreetingHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(args = "TestUser")
class PortsAdapterApplicationTests {

    @Autowired
    private GreetingHistoryRepository greetingHistoryRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void usesJdbcGreetingHistoryByDefault() {
        assertThat(greetingHistoryRepository).isInstanceOf(JdbcGreetingHistoryAdapter.class);
    }

}
