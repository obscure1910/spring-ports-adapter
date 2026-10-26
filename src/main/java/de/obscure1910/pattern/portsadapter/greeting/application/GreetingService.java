package de.obscure1910.pattern.portsadapter.greeting.application;

import de.obscure1910.pattern.portsadapter.greeting.application.port.in.SayHello;
import de.obscure1910.pattern.portsadapter.greeting.application.port.out.notification.UserLoginNotification;
import de.obscure1910.pattern.portsadapter.greeting.application.port.out.notification.UserLoginNotifier;
import de.obscure1910.pattern.portsadapter.greeting.application.port.out.persistence.GreetingHistoryRepository;
import de.obscure1910.pattern.portsadapter.greeting.application.port.out.time.CurrentTimeProvider;
import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GreetingService implements SayHello {

    private final CurrentTimeProvider currentTimeProvider;
    private final UserLoginNotifier userLoginNotifier;
    private final GreetingHistoryRepository historyRepository;

    public GreetingService(GreetingHistoryRepository historyRepository,
                            UserLoginNotifier userLoginNotifier,
                            CurrentTimeProvider currentTimeProvider) {
        this.historyRepository = historyRepository;
        this.userLoginNotifier = userLoginNotifier;
        this.currentTimeProvider = currentTimeProvider;
    }

    @Override
    @Transactional
    public String sayHello(User user) {
        var currentTime = currentTimeProvider.now();
        int visits = this.historyRepository.incrementAndGetVisits(user);
        String message;
        if (visits == 1) {
            message = String.format("Herzlich willkommen zum ersten Mal, %s!", user.name());
        } else {
            message = String.format("Schön dich wiederzusehen, %s! (Besuch #%d)", user.name(), visits);
        }
        userLoginNotifier.notify(new UserLoginNotification(user.name(), currentTime));
        return message;
    }

}
