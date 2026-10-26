package de.obscure1910.pattern.portsadapter.notification.application.port.out;

import de.obscure1910.pattern.portsadapter.notification.domain.model.UserLogin;

public interface LoginEventPublisher {

    void publish(UserLogin userLogin);

}
