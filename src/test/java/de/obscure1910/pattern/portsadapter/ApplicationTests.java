package de.obscure1910.pattern.portsadapter;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ApplicationTests {

    @Test
    void writeDocumentationSnippets() {
        var modules = ApplicationModules.of(PortsAdapterApplication.class).verify();

        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }

}
