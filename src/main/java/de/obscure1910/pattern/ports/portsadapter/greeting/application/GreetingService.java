package de.obscure1910.pattern.ports.portsadapter.greeting.application;

import de.obscure1910.pattern.ports.portsadapter.greeting.application.port.in.SayHello;
import de.obscure1910.pattern.ports.portsadapter.greeting.application.port.out.console.Printer;
import de.obscure1910.pattern.ports.portsadapter.greeting.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class GreetingService implements SayHello {


    private final Printer printer;

    public GreetingService(Printer printer) {
        this.printer = printer;
    }

    @Override
    public void sayHello(User user) {
        this.printer.print(String.format("Hello %s!", user.name()));
    }

}
