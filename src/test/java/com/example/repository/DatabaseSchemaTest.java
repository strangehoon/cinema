package com.example.repository;

import com.example.config.IntegrationRepositoryTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseSchemaTest extends IntegrationRepositoryTest {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbc;

    @Nested
    @DisplayName("운영 DB와 동일한 테스트 스키마")
    class MigratedSchema {
        @Test
        @DisplayName("MySQL에서 Flyway 마이그레이션을 적용한 뒤 저장소 테스트를 실행한다")
        void success_mysql_schema_from_migrations() throws SQLException {
            // given
            String databaseName;
            try (var connection = dataSource.getConnection()) {
                databaseName = connection.getMetaData().getDatabaseProductName();
            }

            // when
            Integer appliedMigrations = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1", Integer.class);

            // then
            assertThat(databaseName).isEqualTo("MySQL");
            assertThat(appliedMigrations).isEqualTo(5);
        }
    }
}
