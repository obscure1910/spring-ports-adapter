package de.obscure1910.pattern.ports.portsadapter.notification.application;

import de.obscure1910.pattern.ports.portsadapter.notification.application.port.in.LoginNotificationCommand;
import de.obscure1910.pattern.ports.portsadapter.notification.application.port.in.NotifyUserLogin;
import de.obscure1910.pattern.ports.portsadapter.notification.application.port.out.LoginEventPublisher;
import de.obscure1910.pattern.ports.portsadapter.notification.domain.model.UserLogin;
import org.springframework.stereotype.Service;

@Service
public class LoginNotificationService implements NotifyUserLogin {

    private final LoginEventPublisher loginEventPublisher;

    public LoginNotificationService(LoginEventPublisher loginEventPublisher) {
        this.loginEventPublisher = loginEventPublisher;
    }

    @Override
    public void notify(LoginNotificationCommand command) {
        UserLogin login = new UserLogin(command.username(), command.loggedInAT());
        this.loginEventPublisher.publish(login);
    }
}
