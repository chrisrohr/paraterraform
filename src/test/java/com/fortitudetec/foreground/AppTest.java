package com.fortitudetec.foreground;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.test.dropwizard.app.DropwizardAppTests.healthCheckNamesOf;
import static org.kiwiproject.test.dropwizard.app.DropwizardAppTests.registeredResourceClassesOf;

import com.fortitudetec.foreground.config.AppConfig;
import com.fortitudetec.foreground.resource.StateResource;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.test.dropwizard.app.PostgresAppTestExtension;

@DisplayName("App")
class AppTest {

    @RegisterExtension
    static final PostgresAppTestExtension<AppConfig> POSTGRES_APP_TEST_EXTENSION =
            new PostgresAppTestExtension<>("migrations.xml", "config-unit-test.yml", App.class);

    private static final DropwizardAppExtension<AppConfig> APP = POSTGRES_APP_TEST_EXTENSION.getApp();

    @Test
    void shouldRegisterResources() {
        assertThat(registeredResourceClassesOf(APP)).contains(
                StateResource.class
        );
    }

    @Test
    void shouldRegisterHealthChecks() {
        assertThat(healthCheckNamesOf(APP)).contains("deadlocks");
    }
}
