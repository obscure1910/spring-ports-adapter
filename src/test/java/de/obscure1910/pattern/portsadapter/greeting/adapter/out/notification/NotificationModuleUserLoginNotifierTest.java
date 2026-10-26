package de.obscure1910.pattern.portsadapter.greeting.adapter.out.notification;

import de.obscure1910.pattern.portsadapter.greeting.application.port.out.notification.UserLoginNotification;
import de.obscure1910.pattern.portsadapter.notification.application.port.in.LoginNotificationCommand;
import de.obscure1910.pattern.portsadapter.notification.application.port.in.NotifyUserLogin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationModuleUserLoginNotifierTest {

    @Mock
    private NotifyUserLogin notifyUserLogin;

    @InjectMocks
    private NotificationModuleUserLoginNotifier userLoginNotifier;

    @Test
    void shouldTranslateAndDelegateLoginNotificationToNotificationModule() {
        Instant loginTime = Instant.parse("2026-10-26T10:00:00Z");

        userLoginNotifier.notify(new UserLoginNotification("Alice", loginTime));

        verify(notifyUserLogin).notify(new LoginNotificationCommand("Alice", loginTime));
    }
}
