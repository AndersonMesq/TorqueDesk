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

import java.util.Optional;
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
    void shouldPersistTenant() {
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

    @Test
    void shouldReturnFalseWhenNoOtherTenantHasSameSlug() {
        Tenant tenant1 = Tenant.builder()
                .name("Tenant Test Slug")
                .slug("tenant-test-slug")
                .status(TenantStatus.ACTIVE)
                .build();
        Tenant tenant2 = Tenant.builder()
                .name("Tenant Test Different Slug")
                .slug("tenant-test-different-slug")
                .status(TenantStatus.ACTIVE)
                .build();
        tenantRepository.save(tenant1);
        tenantRepository.save(tenant2);
        entityManager.flush();
        entityManager.clear();

        boolean exists = tenantRepository.existsBySlugAndIdNot(tenant1.getSlug(), tenant1.getId());
        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAnotherTenantHasSameSlug() {
        Tenant tenant1 = Tenant.builder()
                .name("Tenant Test Slug")
                .slug("tenant-test-slug")
                .status(TenantStatus.ACTIVE)
                .build();
        Tenant tenant2 = Tenant.builder()
                .name("Another Test Slug")
                .slug("another-test-slug")
                .status(TenantStatus.ACTIVE)
                .build();
        tenantRepository.save(tenant1);
        tenantRepository.save(tenant2);
        entityManager.flush();
        entityManager.clear();

        boolean exists = tenantRepository.existsBySlugAndIdNot(tenant1.getSlug(), tenant2.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnTrueWhenSlugExists() {
        Tenant tenant1 = createTenant();
        tenantRepository.save(tenant1);
        entityManager.flush();
        entityManager.clear();

        boolean exists = tenantRepository.existsBySlug(tenant1.getSlug());
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSlugDoesNotExist() {
        String inexistentSlug = "inexistent-slug";

        boolean exists = tenantRepository.existsBySlug(inexistentSlug);
        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindBySlugSuccessfully() {
        Tenant tenant = createTenant();
        tenantRepository.save(tenant);
        entityManager.flush();
        entityManager.clear();

        Optional<Tenant> result = tenantRepository.findBySlug(tenant.getSlug());

        assertThat(result).isPresent().get().satisfies(foundTenant -> {
            assertThat(foundTenant.getName()).isEqualTo(tenant.getName());
            assertThat(foundTenant.getSlug()).isEqualTo(tenant.getSlug());
            assertThat(foundTenant.getStatus()).isEqualTo(tenant.getStatus());
        });
    }

    @Test
    void shouldReturnEmptyWhenSlugDoesNotExist() {
        String nonexistentSlug = "nonexistent-slug";

        Optional<Tenant> result = tenantRepository.findBySlug(nonexistentSlug);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindByNameSuccessfully() {
        Tenant tenant = createTenant();
        tenantRepository.save(tenant);
        entityManager.flush();
        entityManager.clear();

        boolean result = tenantRepository.existsByName(tenant.getName());
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenFindByNameDoesNotExist() {
        String nonexistentName = "Nonexistent Nama";

        boolean result = tenantRepository.existsByName(nonexistentName);

        assertThat(result).isFalse();
    }

    private Tenant createTenant() {
        return Tenant.builder()
                .name("Tenant Test")
                .slug("tenant-test-" + UUID.randomUUID())
                .status(TenantStatus.ACTIVE)
                .build();
    }
}