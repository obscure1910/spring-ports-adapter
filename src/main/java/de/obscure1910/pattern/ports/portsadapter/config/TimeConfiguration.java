package de.obscure1910.pattern.ports.portsadapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class TimeConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

}
