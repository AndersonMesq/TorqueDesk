package com.andersonmesq.TorqueDesk.integration.repository;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class PostgresSQLContainerIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("torquedesk_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Test
    void shouldStartPostgreSQLContainer() {
        assertThat(postgres.isRunning()).isTrue();
    }
}