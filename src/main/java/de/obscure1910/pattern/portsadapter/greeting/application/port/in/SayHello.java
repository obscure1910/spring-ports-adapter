package de.obscure1910.pattern.portsadapter.greeting.application.port.in;

import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;

public interface SayHello {

    void sayHello(User user);

}
