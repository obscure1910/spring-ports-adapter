package de.obscure1910.pattern.portsadapter.greeting.adapter.in;

import de.obscure1910.pattern.portsadapter.greeting.adapter.out.persistence.InMemoryGreetingHistoryAdapter;
import de.obscure1910.pattern.portsadapter.greeting.application.port.in.SayHello;
import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GreetingCliAdapterTest {

    @Mock
    private SayHello sayHello;

    @Spy
    InMemoryGreetingHistoryAdapter historyRepository;

    @InjectMocks
    private GreetingCliAdapter adapter;

    @Test
    void shouldCallPortWithGivenUsername() {
        adapter.run("Alice");

        verify(sayHello).sayHello(new User("Alice"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldThrowExceptionWhenUsernameIsMissingOrBlank(String username) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adapter.run(username)
        );

        assertEquals("Username argument is required.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNoArgsProvided() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adapter.run()
        );

        assertEquals("Username argument is required.", exception.getMessage());
    }
}
