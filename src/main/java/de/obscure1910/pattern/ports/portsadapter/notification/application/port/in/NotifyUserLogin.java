package de.obscure1910.pattern.ports.portsadapter.notification.application.port.in;

public interface NotifyUserLogin {

    void notify(LoginNotificationCommand command);

}
