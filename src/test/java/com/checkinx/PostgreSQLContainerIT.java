package com.checkinx;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSQLContainerIT {
    @Rule
    public PostgreSQLContainer postgresContainer = new PostgreSQLContainer();

    @Test
    public void whenSelectQueryExecuted_thenResultsReturned()
        throws Exception {

        String jdbcUrl = postgresContainer.getJdbcUrl();
        String username = postgresContainer.getUsername();
        String password = postgresContainer.getPassword();

        Connection conn = DriverManager
            .getConnection(jdbcUrl, username, password);

        ResultSet resultSet =
            conn.createStatement().executeQuery("SELECT 1");
        resultSet.next();

        int result = resultSet.getInt(1);

        assertEquals(1, result);
    }
}
