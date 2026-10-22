package com.andersonmesq.TorqueDesk.integration.service;

import com.andersonmesq.TorqueDesk.admin.dto.TenantProvisionResponse;
import com.andersonmesq.TorqueDesk.admin.exception.DuplicateSlugException;
import com.andersonmesq.TorqueDesk.admin.service.TenantProvisioningService;
import com.andersonmesq.TorqueDesk.shared.util.SlugGenerator;
import com.andersonmesq.TorqueDesk.tenant.dto.CreateTenantRequest;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import com.andersonmesq.TorqueDesk.user.exception.OwnerAlreadyExistException;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import com.andersonmesq.TorqueDesk.usertenant.role.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class TenantProvisioningServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    TenantProvisioningService tenantProvisioningService;

    @Autowired
    TenantRepository tenantRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserTenantRepository userTenantRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userTenantRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void shouldCreateTenantSuccessfully() {
        CreateTenantRequest request = new CreateTenantRequest(
                "Test Tenant",
                "John test",
                "john.test@email.com",
                "123456789"
        );

        TenantProvisionResponse response = tenantProvisioningService.createTenant(request);

        assertThat(response).isNotNull().satisfies(result -> {
            assertThat(result.tenantId()).isNotNull();
            assertThat(result.ownerId()).isNotNull();
            assertThat(result.temporaryPassword()).isNotNull();
        });
        Tenant persistedTenant = tenantRepository.findById(response.tenantId()).orElseThrow();
        assertThat(persistedTenant.getName()).isEqualTo(request.companyName());
        String expectedSlug = SlugGenerator.generate(request.companyName());
        assertThat(persistedTenant.getSlug()).isEqualTo(expectedSlug);
        assertThat(persistedTenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        User persistedOwner = userRepository.findById(response.ownerId()).orElseThrow();
        assertThat(persistedOwner.getFullName()).isEqualTo(request.ownerName());
        assertThat(persistedOwner.getEmail()).isEqualTo(request.ownerEmail());
        assertThat(passwordEncoder.matches(response.temporaryPassword(), persistedOwner.getPassword())).isTrue();
        assertThat(persistedOwner.getEnabled()).isTrue();
        UserTenant persistedUserTenant = userTenantRepository.findByUserIdAndTenantId(response.ownerId(), response.tenantId()).orElseThrow();
        assertThat(persistedUserTenant.getUser().getId()).isEqualTo(persistedOwner.getId());
        assertThat(persistedUserTenant.getTenant().getId()).isEqualTo(persistedTenant.getId());
        assertThat(persistedUserTenant.getRole()).isEqualTo(Role.OWNER);
        assertThat(persistedUserTenant.getEnabled()).isTrue();
    }

    @Test
    void shouldRejectTenantCreationWhenSlugAlreadyExists() {
        CreateTenantRequest firstRequest = new CreateTenantRequest(
                "Test Tenant 1",
                "John test",
                "john.test@email.com",
                "123456789"
        );
        TenantProvisionResponse response = tenantProvisioningService.createTenant(firstRequest);
        CreateTenantRequest secondRequest = new CreateTenantRequest(
                "Test Tenant 1",
                "Bob test",
                "bob.test@email.com",
                "123456789"
        );

        assertThatThrownBy(() -> tenantProvisioningService.createTenant(secondRequest))
                .isInstanceOf(DuplicateSlugException.class).hasMessage("Tenant already exists");
        Tenant savedTenant = tenantRepository.findById(response.tenantId()).orElseThrow();
        assertThat(tenantRepository.findAll()).hasSize(1);
        assertThat(userRepository.findAll()).hasSize(1);
        assertThat(userTenantRepository.findAll()).hasSize(1);
        assertThat(tenantRepository.findBySlug(savedTenant.getSlug()))
                .isPresent()
                .get()
                .extracting(Tenant::getName)
                .isEqualTo(firstRequest.companyName());
    }

    @Test
    void shouldRejectTenantCreationWhenEmailAlreadyExists() {
        CreateTenantRequest firstRequest = new CreateTenantRequest(
                "Test Tenant 1",
                "John test",
                "john.test@email.com",
                "123456789"
        );
        TenantProvisionResponse response = tenantProvisioningService.createTenant(firstRequest);
        CreateTenantRequest secondRequest = new CreateTenantRequest(
                "Test Tenant 2",
                "John test",
                "john.test@email.com",
                "123456789"
        );

        assertThatThrownBy(() -> tenantProvisioningService.createTenant(secondRequest))
                .isInstanceOf(OwnerAlreadyExistException.class).hasMessage("Owner with this email already exists");
        User savedOwner = userRepository.findById(response.ownerId()).orElseThrow();
        assertThat(tenantRepository.findAll()).hasSize(1);
        assertThat(userRepository.findAll()).hasSize(1);
        assertThat(userTenantRepository.findAll()).hasSize(1);
        assertThat(userRepository.findByEmail(savedOwner.getEmail()))
                .isPresent()
                .get()
                .extracting(User::getEmail)
                .isEqualTo(firstRequest.ownerEmail());
    }
}