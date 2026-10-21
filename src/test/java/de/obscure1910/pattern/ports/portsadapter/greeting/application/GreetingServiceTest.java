package de.obscure1910.pattern.ports.portsadapter.greeting.application;

import de.obscure1910.pattern.ports.portsadapter.greeting.application.port.out.console.Printer;
import de.obscure1910.pattern.ports.portsadapter.greeting.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GreetingServiceTest {

    @Mock
    private Printer printer;

    @InjectMocks
    private GreetingService greetingService;

    @Test
    void shouldSayHelloViaPrinterPort() {
        User user = new User("Alice");

        greetingService.sayHello(user);

        verify(printer).print("Hello Alice!");
    }
}
