package com.example.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class MySqlIntegrationTest {
    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MySqlTestDatabase::url);
        registry.add("spring.datasource.username", MySqlTestDatabase::username);
        registry.add("spring.datasource.password", MySqlTestDatabase::password);
    }
}
