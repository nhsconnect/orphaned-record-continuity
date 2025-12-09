package uk.nhs.prm.deductions.pdsadaptor;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.util.Map;

public class WireMockInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        WireMockServer wireMockServer = new WireMockServer(options().gzipDisabled(true).port(8080));
        wireMockServer.start();

        configurableApplicationContext
                .getBeanFactory()
                .registerSingleton("wireMockServer", wireMockServer);

        configurableApplicationContext.addApplicationListener(applicationEvent -> {
            if (applicationEvent instanceof ContextClosedEvent) {
                wireMockServer.stop();
            }
        });

        TestPropertyValues
                .of(Map.of("todo_base_url", "https://localhost:" + 8080))
                .applyTo(configurableApplicationContext);
    }
}
