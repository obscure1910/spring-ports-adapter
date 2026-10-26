package de.obscure1910.pattern.portsadapter.greeting.adapter.out.console;

import de.obscure1910.pattern.portsadapter.greeting.application.port.out.console.Printer;
import org.springframework.stereotype.Component;


@Component
public class SystemOutPrinter implements Printer {

    @Override
    public void print(String message) {
        IO.println(message);
    }
}
