package de.obscure1910.pattern.ports.portsadapter.greeting.adapter.out.notification;

import de.obscure1910.pattern.ports.portsadapter.greeting.application.port.out.notification.UserLoginNotification;
import de.obscure1910.pattern.ports.portsadapter.greeting.application.port.out.notification.UserLoginNotifier;
import de.obscure1910.pattern.ports.portsadapter.notification.application.port.in.LoginNotificationCommand;
import de.obscure1910.pattern.ports.portsadapter.notification.application.port.in.NotifyUserLogin;
import org.springframework.stereotype.Component;

@Component
public class NotificationModuleUserLoginNotifier implements UserLoginNotifier {

    private final NotifyUserLogin notifyUserLogin;

    public NotificationModuleUserLoginNotifier(NotifyUserLogin notifyUserLogin) {
        this.notifyUserLogin = notifyUserLogin;
    }

    @Override
    public void notify(UserLoginNotification notification) {
        notifyUserLogin.notify(
                new LoginNotificationCommand(notification.username(), notification.loggedInAt())
        );
    }
}
