package de.obscure1910.pattern.ports.portsadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class PortsAdapterApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(PortsAdapterApplication.class, args);
        int exitCode = SpringApplication.exit(context);
        System.exit(exitCode);
    }


}
