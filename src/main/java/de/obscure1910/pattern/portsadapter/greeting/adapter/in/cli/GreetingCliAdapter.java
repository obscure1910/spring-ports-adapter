package de.obscure1910.pattern.portsadapter.greeting.adapter.in.cli;

import de.obscure1910.pattern.portsadapter.greeting.application.port.in.SayHello;
import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.cli.enabled", havingValue = "true")
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
        IO.println(sayHello.sayHello(new User(args[0])));
    }
}
