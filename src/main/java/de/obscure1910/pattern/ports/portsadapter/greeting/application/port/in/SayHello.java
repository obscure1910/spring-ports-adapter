package de.obscure1910.pattern.ports.portsadapter.greeting.application.port.in;

import de.obscure1910.pattern.ports.portsadapter.greeting.domain.model.User;

public interface SayHello {

    void sayHello(User user);

}
