package de.obscure1910.pattern.portsadapter.greeting.application;

import de.obscure1910.pattern.portsadapter.greeting.application.port.out.persistence.GreetingHistoryRepository;
import de.obscure1910.pattern.portsadapter.greeting.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class GreetingServiceTransactionTest {

    @Autowired
    private GreetingService greetingService;

    @MockitoBean
    private GreetingHistoryRepository greetingHistoryRepository;

    @Test
    void allocatesTheVisitNumberInsideTheApplicationTransaction() {
        AtomicBoolean transactionWasActive = new AtomicBoolean();
        when(greetingHistoryRepository.incrementAndGetVisits(new User("Ada")))
                .thenAnswer(invocation -> {
                    transactionWasActive.set(TransactionSynchronizationManager.isActualTransactionActive());
                    return 1;
                });

        assertThat(greetingService.sayHello(new User("Ada")))
                .isEqualTo("Herzlich willkommen zum ersten Mal, Ada!");

        assertThat(transactionWasActive).isTrue();
    }
}
