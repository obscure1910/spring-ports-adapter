package de.obscure1910.pattern.portsadapter.notification.adapter.out.console;

import de.obscure1910.pattern.portsadapter.notification.application.port.out.LoginEventPublisher;
import de.obscure1910.pattern.portsadapter.notification.domain.model.UserLogin;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
public class ConsoleThirdSystemAdapter implements LoginEventPublisher {

    @Override
    public void publish(UserLogin userLogin) {
        var loginTime = userLogin.loggedInAt().truncatedTo(ChronoUnit.SECONDS);
        IO.println(String.format("Drittsystem informiert: user=%s, loggedInAt=%s", userLogin.username(), loginTime));
    }
}
