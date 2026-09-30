package com.andersonmesq.TorqueDesk.integration.service;

import com.andersonmesq.TorqueDesk.admin.exception.DuplicateSlugException;
import com.andersonmesq.TorqueDesk.admin.service.AdminTenantService;
import com.andersonmesq.TorqueDesk.tenant.dto.TenantResponse;
import com.andersonmesq.TorqueDesk.tenant.dto.UpdateTenantRequest;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyActiveException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyDeactivatedException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantNotFoundException;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class AdminTenantServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private AdminTenantService adminTenantService;

    @Autowired
    private TenantRepository tenantRepository;

    @AfterEach
    void tearDown() {
        tenantRepository.deleteAll();
    }

    @Test
    void shouldFindByIdSuccessfully() {
        Tenant tenant = createAndSaveTenant();

        TenantResponse response = adminTenantService.findById(tenant.getId());

        assertThat(response).isNotNull().satisfies(result -> {
            assertThat(result.id()).isEqualTo(tenant.getId());
            assertThat(result.name()).isEqualTo(tenant.getName());
            assertThat(result.slug()).isEqualTo(tenant.getSlug());
            assertThat(result.status()).isEqualTo(tenant.getStatus());
        });
    }

    @Test
    void shouldRejectFindByIdWhenTenantNotExists() {
        UUID nonExistentTenantId = UUID.randomUUID();

        assertThatThrownBy(() -> adminTenantService.findById(nonExistentTenantId))
                .isInstanceOf(TenantNotFoundException.class).hasMessage("Tenant not found");
    }

    @Test
    void shouldReturnFindAllSuccessfully() {
        Tenant tenant1 = createAndSaveTenant();
        Tenant tenant2 = createAndSaveTenant();
        Pageable pageable = PageRequest.of(0, 10);

        Page<TenantResponse> response = adminTenantService.findAll(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent()).extracting(TenantResponse::id)
                .containsExactlyInAnyOrder(tenant1.getId(), tenant2.getId());
    }

    @Test
    void shouldUpdateTenantSuccessfully(){
        Tenant tenant = createAndSaveTenant();
        UpdateTenantRequest request = new UpdateTenantRequest(
                "NewCompanyName"
        );

        TenantResponse response = adminTenantService.update(tenant.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(request.companyName());
        assertThat(response.slug()).isEqualTo("newcompanyname");
        Tenant updateTenant = tenantRepository.findById(response.id()).orElseThrow();
        assertThat(updateTenant.getName()).isEqualTo(request.companyName());
        assertThat(updateTenant.getSlug()).isEqualTo("newcompanyname");
    }

    @Test
    void shouldRejectUpdateWhenSlugAlreadyExists(){
        Tenant tenant1 = Tenant.builder()
                .name("Duplicated Slug Test")
                .slug("duplicated-slug-test")
                .status(TenantStatus.ACTIVE)
                .build();
        Tenant tenant2 = createAndSaveTenant();
        tenantRepository.saveAndFlush(tenant1);
        UpdateTenantRequest request = new UpdateTenantRequest(
                "Duplicated Slug Test"
        );
        String originalName = tenant2.getName();
        String originalSlug = tenant2.getSlug();

        assertThatThrownBy(() -> adminTenantService.update(tenant2.getId(), request))
                .isInstanceOf(DuplicateSlugException.class).hasMessage("Slug already exists");
        Tenant unchangedTenant = tenantRepository.findById(tenant2.getId()).orElseThrow();
        assertThat(unchangedTenant.getName()).isEqualTo(originalName);
        assertThat(unchangedTenant.getSlug()).isEqualTo(originalSlug);
    }

    @Test
    void shouldDeactivateTenantSuccessfully(){
        Tenant tenant = createAndSaveTenant();

        adminTenantService.deactivate(tenant.getId());

        Tenant deactivatedTenant = tenantRepository.findById(tenant.getId()).orElseThrow();
        assertThat(deactivatedTenant.getStatus()).isEqualTo(TenantStatus.INACTIVE);
    }

    @Test
    void shouldRejectDeactivateWhenTenantAlreadyDeactivated(){
        Tenant tenant = createAndSaveTenant();
        adminTenantService.deactivate(tenant.getId());

        assertThatThrownBy(() -> adminTenantService.deactivate(tenant.getId()))
                .isInstanceOf(TenantAlreadyDeactivatedException.class).hasMessage("Tenant already deactivate");
    }

    @Test
    void shouldActivateTenantSuccessfully(){
        Tenant tenant = Tenant.builder()
                .name("Test Tenant")
                .slug("test-tenant-" + UUID.randomUUID())
                .status(TenantStatus.INACTIVE)
                .build();
        tenantRepository.saveAndFlush(tenant);

        adminTenantService.activate(tenant.getId());

        Tenant activatedTenant = tenantRepository.findById(tenant.getId()).orElseThrow();
        assertThat(activatedTenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    void shouldRejectActivateWhenTenantAlreadyActivated(){
        Tenant tenant = Tenant.builder()
                .name("Test Tenant")
                .slug("test-tenant-" + UUID.randomUUID())
                .status(TenantStatus.INACTIVE)
                .build();
        tenantRepository.saveAndFlush(tenant);

        adminTenantService.activate(tenant.getId());

        assertThatThrownBy(() -> adminTenantService.activate(tenant.getId()))
                .isInstanceOf(TenantAlreadyActiveException.class).hasMessage("Tenant already active");
    }

    private Tenant createAndSaveTenant() {
        Tenant tenant = Tenant.builder()
                .name("Test Tenant")
                .slug("test-tenant-" + UUID.randomUUID())
                .status(TenantStatus.ACTIVE)
                .build();
        return tenantRepository.saveAndFlush(tenant);
    }
}