package de.obscure1910.pattern.ports.portsadapter.greeting.application.port.out.time;

import java.time.Instant;

public interface CurrentTimeProvider {

    Instant now();

}
