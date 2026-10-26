package de.obscure1910.pattern.ports.portsadapter.notification.application;

import de.obscure1910.pattern.ports.portsadapter.notification.application.port.in.LoginNotificationCommand;
import de.obscure1910.pattern.ports.portsadapter.notification.application.port.out.LoginEventPublisher;
import de.obscure1910.pattern.ports.portsadapter.notification.domain.model.UserLogin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LoginNotificvationServiceTest {

    @Mock
    LoginEventPublisher loginEventPublisher;

    @InjectMocks
    LoginNotificationService loginNotificationService;

    @Test
    void shouldPublishEventViaEventPublisher() {
        Instant now = Instant.now();
        LoginNotificationCommand command = new LoginNotificationCommand("Alice", now);

        loginNotificationService.notify(command);

        verify(loginEventPublisher).publish(new UserLogin("Alice", now));
    }

}
