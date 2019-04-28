package com.checkinx;

import java.time.Duration;

import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import com.checkinx.demo2.Application;

@ContextConfiguration(initializers = {CheckInxContextInitializer.class})
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {
    static final PostgreSQLContainer postgreSQLContainer =
        (PostgreSQLContainer) new PostgreSQLContainer("postgres:10.6")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres")
            .withStartupTimeout(Duration.ofSeconds(600));

    /**
     * static block instead of @ClassRule used to workaround shutting down of container after each test class executed
     */
    static {
        postgreSQLContainer.start();
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private Flyway flyway;

    @After
    public void cleanTestDatabase() {
        jdbcTemplate.execute("DROP SCHEMA public CASCADE; "
            + "CREATE SCHEMA public;"
            + "GRANT ALL ON SCHEMA public TO postgres;"
            + "GRANT ALL ON SCHEMA public TO public;");
        flyway.migrate();
    }
}
