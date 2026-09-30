package com.andersonmesq.TorqueDesk.integration.repository;

import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TenantRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistTenant(){
        Tenant tenant = createTenant();

        Tenant savedTenant = tenantRepository.save(tenant);
        entityManager.flush();
        entityManager.clear();

        Tenant persistedTenant = tenantRepository.findById(savedTenant.getId()).orElseThrow();
        assertThat(savedTenant.getId()).isNotNull();
        assertThat(savedTenant.getId()).isEqualTo(persistedTenant.getId());
        assertThat(savedTenant.getName()).isEqualTo(persistedTenant.getName());
        assertThat(savedTenant.getSlug()).isEqualTo(persistedTenant.getSlug());
        assertThat(savedTenant.getStatus()).isEqualTo(persistedTenant.getStatus());
    }



    private Tenant createTenant(){
        return new Tenant(
                UUID.randomUUID(),
                "Tenant Test",
                "tenant-test-" + UUID.randomUUID(),
                TenantStatus.ACTIVE
        );
    }
}
