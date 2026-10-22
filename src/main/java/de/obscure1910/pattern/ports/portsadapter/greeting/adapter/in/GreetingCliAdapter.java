package de.obscure1910.pattern.ports.portsadapter.greeting.adapter.in;

import de.obscure1910.pattern.ports.portsadapter.greeting.application.port.in.SayHello;
import de.obscure1910.pattern.ports.portsadapter.greeting.domain.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GreetingCliAdapter implements CommandLineRunner {

    private final SayHello sayHello;

    public GreetingCliAdapter(SayHello sayHello) {
        this.sayHello = sayHello;
    }

    @Override
    public void run(String... args) {
        if (args.length == 0 || args[0] == null || args[0].isBlank()) {
            throw new IllegalArgumentException("Username argument is required.");
        }
        sayHello.sayHello(new User(args[0]));
    }
}
