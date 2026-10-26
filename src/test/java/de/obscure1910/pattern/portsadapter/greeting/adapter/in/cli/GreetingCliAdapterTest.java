package de.obscure1910.pattern.portsadapter.greeting.adapter.in.cli;

import de.obscure1910.pattern.portsadapter.greeting.application.port.in.SayHello;
import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class GreetingCliAdapterTest {

    @Mock
    private SayHello sayHello;

    @InjectMocks
    private GreetingCliAdapter adapter;

    @Test
    void shouldPrintGreetingReturnedByApplicationPort(CapturedOutput output) {
        when(sayHello.sayHello(new User("Alice")))
                .thenReturn("Herzlich willkommen zum ersten Mal, Alice!");

        adapter.run("Alice");

        assertTrue(output.getOut().contains("Herzlich willkommen zum ersten Mal, Alice!"));
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
