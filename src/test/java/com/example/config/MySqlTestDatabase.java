package com.example.config;

import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.MySQLContainer;

public final class MySqlTestDatabase {
    private static final MySQLContainer<?> CONTAINER = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("cinema_test")
            .withUsername("cinema")
            .withPassword("cinema");

    static {
        CONTAINER.start();
        Flyway.configure().dataSource(url(), username(), password()).load().migrate();
    }

    private MySqlTestDatabase() {
    }

    public static String url() {
        return CONTAINER.getJdbcUrl();
    }

    public static String username() {
        return CONTAINER.getUsername();
    }

    public static String password() {
        return CONTAINER.getPassword();
    }

    public static JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(new DriverManagerDataSource(url(), username(), password()));
    }
}
