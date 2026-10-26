package de.obscure1910.pattern.portsadapter.greeting.application;

import de.obscure1910.pattern.portsadapter.greeting.adapter.out.persistence.InMemoryGreetingHistoryAdapter;
import de.obscure1910.pattern.portsadapter.greeting.application.port.out.notification.UserLoginNotification;
import de.obscure1910.pattern.portsadapter.greeting.application.port.out.notification.UserLoginNotifier;
import de.obscure1910.pattern.portsadapter.greeting.application.port.out.time.CurrentTimeProvider;
import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GreetingServiceTest {

    @Mock
    private UserLoginNotifier userLoginNotifier;

    @Mock
    private CurrentTimeProvider currentTimeProvider;

    @Spy
    private InMemoryGreetingHistoryAdapter historyRepository;

    @InjectMocks
    private GreetingService greetingService;

    @Test
    void shouldReturnGreetingForFirstAndReturningVisit() {
        User user = new User("Alice");

        var result1 = greetingService.sayHello(user);
        var result2 = greetingService.sayHello(user);

        Assertions.assertEquals("Herzlich willkommen zum ersten Mal, Alice!", result1);
        Assertions.assertEquals("Schön dich wiederzusehen, Alice! (Besuch #2)", result2);
    }

    @Test
    void shouldNotifyUserLoginViaGreetingNotificationPort() {
        Instant loginTime = Instant.parse("2026-10-26T10:00:00Z");
        when(currentTimeProvider.now()).thenReturn(loginTime);

        greetingService.sayHello(new User("Alice"));

        verify(userLoginNotifier).notify(new UserLoginNotification("Alice", loginTime));
    }

}
