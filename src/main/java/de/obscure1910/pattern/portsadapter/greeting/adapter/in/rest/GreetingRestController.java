package de.obscure1910.pattern.portsadapter.greeting.adapter.in.rest;

import de.obscure1910.pattern.portsadapter.greeting.application.port.in.SayHello;
import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingRestController {

    private final SayHello sayHello;

    public GreetingRestController(SayHello sayHello) {
        this.sayHello = sayHello;
    }

    @GetMapping(value = "/greeting", produces = MediaType.TEXT_PLAIN_VALUE)
    public String greeting(@RequestParam(required = false) String name) {
        if (name == null || name.isBlank()) {
            return "Wie heißt die zu grüßende Person?";
        }

        return sayHello.sayHello(new User(name));
    }
}
