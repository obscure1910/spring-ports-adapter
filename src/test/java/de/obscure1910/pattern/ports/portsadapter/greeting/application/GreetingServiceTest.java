package de.obscure1910.pattern.ports.portsadapter.greeting.application;

import de.obscure1910.pattern.ports.portsadapter.greeting.adapter.out.persistence.InMemoryGreetingHistoryAdapter;
import de.obscure1910.pattern.ports.portsadapter.greeting.application.port.out.console.Printer;
import de.obscure1910.pattern.ports.portsadapter.greeting.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GreetingServiceTest {

    @Mock
    private Printer printer;

    @Spy
    private InMemoryGreetingHistoryAdapter historyRepository;

    @InjectMocks
    private GreetingService greetingService;

    @Test
    void shouldSayHelloAndHelloAgainViaPrinterPort() {
        User user = new User("Alice");

        greetingService.sayHello(user);
        greetingService.sayHello(user);

        InOrder inOrder = Mockito.inOrder(printer);

        inOrder.verify(printer).print("Herzlich willkommen zum ersten Mal, Alice!");
        inOrder.verify(printer).print("Schön dich wiederzusehen, Alice! (Besuch #2)");
    }

}
