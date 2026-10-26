package de.obscure1910.pattern.ports.portsadapter.notification.application.port.out;

import de.obscure1910.pattern.ports.portsadapter.notification.domain.model.UserLogin;

public interface LoginEventPublisher {

    void publish(UserLogin userLogin);

}
