package de.obscure1910.pattern.portsadapter.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "de.obscure1910.pattern.portsadapter", importOptions = ImportOption.DoNotIncludeTests.class)
public class PortsAdapterArchitectureTest {

    private static final String GREETING = "de.obscure1910.pattern.portsadapter.greeting..";
    private static final String NOTIFICATION = "de.obscure1910.pattern.portsadapter.notification..";

    private static final String GREETING_OUTBOUND_ADAPTER_NOTIFICATION = "de.obscure1910.pattern.portsadapter.greeting.adapter.out.notification..";
    private static final String NOTIFICATION_INBOUND_APPLICATION_PORT = "de.obscure1910.pattern.portsadapter.notification.application.port.in..";

    @ArchTest
    public static final ArchRule onlyNotificationOutboundAdapterMayDependOnNotification =
            classes()
                    .that().resideInAPackage(GREETING)
                    .and().resideOutsideOfPackage(GREETING_OUTBOUND_ADAPTER_NOTIFICATION)
                    .should().onlyDependOnClassesThat(
                            resideOutsideOfPackage(NOTIFICATION)
                    );

    @ArchTest
    static final ArchRule notificationOutboundAdapterMayUseOnlyNotificationInboundPort =
            classes()
                    .that().resideInAPackage(GREETING_OUTBOUND_ADAPTER_NOTIFICATION)
                    .should().onlyDependOnClassesThat(
                            resideOutsideOfPackage(NOTIFICATION).
                                    or(resideInAPackage(NOTIFICATION_INBOUND_APPLICATION_PORT))
                    );

}
