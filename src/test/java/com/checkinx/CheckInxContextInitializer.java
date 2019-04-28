package com.checkinx;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;

public class CheckInxContextInitializer
    implements org.springframework.context.ApplicationContextInitializer<ConfigurableApplicationContext> {

    public void initialize(ConfigurableApplicationContext context) {
        TestPropertyValues.of(
            "spring.datasource.url=" + AbstractIntegrationTest.postgreSQLContainer.getJdbcUrl(),
            "spring.datasource.username=" + AbstractIntegrationTest.postgreSQLContainer.getUsername(),
            "spring.datasource.password=" + AbstractIntegrationTest.postgreSQLContainer.getPassword()
        ).applyTo(context.getEnvironment());
    }
}
