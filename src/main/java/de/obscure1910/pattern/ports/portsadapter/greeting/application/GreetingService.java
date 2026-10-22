package de.obscure1910.pattern.ports.portsadapter.greeting.application;

import de.obscure1910.pattern.ports.portsadapter.greeting.application.port.in.SayHello;
import de.obscure1910.pattern.ports.portsadapter.greeting.application.port.out.console.Printer;
import de.obscure1910.pattern.ports.portsadapter.greeting.application.port.out.persistence.GreetingHistoryRepository;
import de.obscure1910.pattern.ports.portsadapter.greeting.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class GreetingService implements SayHello {

    private final GreetingHistoryRepository historyRepository;
    private final Printer printer;

    public GreetingService(Printer printer, GreetingHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
        this.printer = printer;
    }

    @Override
    public void sayHello(User user) {
        int visits = this.historyRepository.incrementAndGetVisits(user);
        if (visits == 1) {
            this.printer.print(String.format("Herzlich willkommen zum ersten Mal, %s!", user.name()));
        } else {
            this.printer.print(String.format("Schön dich wiederzusehen, %s! (Besuch #%d)", user.name(), visits));
        }
    }

}
