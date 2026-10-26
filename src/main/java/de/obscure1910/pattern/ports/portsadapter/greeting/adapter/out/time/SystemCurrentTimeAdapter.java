package de.obscure1910.pattern.ports.portsadapter.greeting.adapter.out.time;

import de.obscure1910.pattern.ports.portsadapter.greeting.application.port.out.time.CurrentTimeProvider;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class SystemCurrentTimeAdapter implements CurrentTimeProvider {

    private final Clock clock;

    public SystemCurrentTimeAdapter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Instant now() {
        return Instant.now(this.clock);
    }

}
