package de.obscure1910.pattern.portsadapter;

import de.obscure1910.pattern.portsadapter.greeting.adapter.out.persistence.InMemoryGreetingHistoryAdapter;
import de.obscure1910.pattern.portsadapter.greeting.application.port.out.persistence.GreetingHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("in-memory")
@SpringBootTest
class InMemoryGreetingHistoryProfileTest {

    @Autowired
    private GreetingHistoryRepository greetingHistoryRepository;

    @Test
    void usesTheInMemoryAdapterWhenTheInMemoryProfileIsActive() {
        assertThat(greetingHistoryRepository).isInstanceOf(InMemoryGreetingHistoryAdapter.class);
    }
}
