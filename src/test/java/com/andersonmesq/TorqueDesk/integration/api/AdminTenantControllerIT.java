package com.andersonmesq.TorqueDesk.integration.api;

import com.andersonmesq.TorqueDesk.shared.util.SlugGenerator;
import com.andersonmesq.TorqueDesk.tenant.dto.CreateTenantRequest;
import com.andersonmesq.TorqueDesk.tenant.dto.UpdateTenantRequest;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class AdminTenantControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTenantRepository userTenantRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void tearDown() {
        userTenantRepository.deleteAll();
        tenantRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateTenantSuccessfully() throws Exception {
        CreateTenantRequest request = new CreateTenantRequest(
                "Test Tenant",
                "John test",
                "john.test@email.com",
                "123456789"
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/api/v1/admin/tenants")
                .with(user("test-user").roles("SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").isNotEmpty())
                .andExpect(jsonPath("$.ownerId").isNotEmpty())
                .andExpect(jsonPath("$.temporaryPassword").isNotEmpty());
        assertThat(tenantRepository.findAll()).hasSize(1).first().satisfies(tenant -> {
            assertThat(tenant.getId()).isNotNull();
            assertThat(tenant.getName()).isEqualTo(request.companyName());
            assertThat(tenant.getSlug()).isEqualTo(SlugGenerator.generate(request.companyName()));
            assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        });
        assertThat(userRepository.findAll()).hasSize(1).first().satisfies(user -> {
            assertThat(user.getId()).isNotNull();
            assertThat(user.getFullName()).isEqualTo(request.ownerName());
            assertThat(user.getEmail()).isEqualTo(request.ownerEmail());
            assertThat(user.getPassword()).isNotNull();
        });
    }

    @Test
    void shouldRejectCreateTenantWhenUserIsNotAuthenticated() throws Exception {
        CreateTenantRequest request = new CreateTenantRequest(
                "Test Tenant",
                "John test",
                "john.test@email.com",
                "123456789"
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/api/v1/admin/tenants")
                .with(user("test-user"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        result.andExpect(status().isForbidden());
        assertThat(tenantRepository.findAll()).isEmpty();
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectCreateTenantWhenRequestIsInvalid() throws Exception {
        CreateTenantRequest request = new CreateTenantRequest(
                "",
                "John test",
                "john.test@email.com",
                "123456789"
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/api/v1/admin/tenants")
                .with(user("test-user").roles("SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        result.andExpect(status().isBadRequest());
        assertThat(tenantRepository.findAll()).isEmpty();
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectCreateTenantWhenEmailIsInvalid() throws Exception {
        CreateTenantRequest request = new CreateTenantRequest(
                "Test Tenant",
                "John test",
                "john.test.com",
                "123456789"
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/api/v1/admin/tenants")
                .with(user("test-user").roles("SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        result.andExpect(status().isBadRequest());
        assertThat(tenantRepository.findAll()).isEmpty();
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void shouldFindByIdSuccessfully() throws Exception {
        Tenant tenant = createAndSaveTenant("Tenant Name", "tenant-name");

        ResultActions result = mockMvc.perform(get("/api/v1/admin/tenants/{id}", tenant.getId())
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenant.getId().toString()))
                .andExpect(jsonPath("$.name").value(tenant.getName()))
                .andExpect(jsonPath("$.slug").isNotEmpty())
                .andExpect(jsonPath("$.status").value(tenant.getStatus().toString()));
    }

    @Test
    void shouldRejectWhenTenantIdNotExists() throws Exception {
        UUID tenantId = UUID.randomUUID();

        ResultActions result = mockMvc.perform(get("/api/v1/admin/tenants/{id}", tenantId)
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isNotFound());
    }

    @Test
    void shouldFindAllTenantsSuccessfully() throws Exception {
        Tenant tenant1 = createAndSaveTenant("Tenant One", "tenant-one");
        Tenant tenant2 = createAndSaveTenant("Tenant Two", "tenant-two");

        ResultActions result = mockMvc.perform(get("/api/v1/admin/tenants")
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(tenant1.getId().toString()))
                .andExpect(jsonPath("$.content[0].name").value(tenant1.getName()))
                .andExpect(jsonPath("$.content[0].slug").value(tenant1.getSlug()))
                .andExpect(jsonPath("$.content[1].id").value(tenant2.getId().toString()))
                .andExpect(jsonPath("$.content[1].name").value(tenant2.getName()))
                .andExpect(jsonPath("$.content[1].slug").value(tenant2.getSlug()));
    }

    @Test
    void shouldReturnTenantsAccordingToPaginationParameters() throws Exception {
        Tenant tenant1 = createAndSaveTenant("Alpha Company", "alpha-company");
        createAndSaveTenant("Beta Company", "beta-company");

        ResultActions result = mockMvc.perform(get("/api/v1/admin/tenants")
                        .param("page", "0")
                        .param("size", "1")
                        .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(tenant1.getId().toString()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void shouldDeactivateTenantSuccessfully() throws Exception {
        Tenant tenant = createAndSaveTenant("Tenant Name", "tenant-name");

        ResultActions result = mockMvc.perform(patch("/api/v1/admin/tenants/{id}/deactivate", tenant.getId())
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isNoContent());
        Tenant updatedTenant = tenantRepository.findById(tenant.getId()).orElseThrow();
        assertThat(updatedTenant.getStatus()).isEqualTo(TenantStatus.INACTIVE);
    }

    @Test
    void shouldRejectDeactivateWhenTenantAlreadyInactive() throws Exception {
        Tenant tenant = createAndSaveTenant("Tenant Name", "tenant-name");
        tenant.setStatus(TenantStatus.INACTIVE);
        tenantRepository.saveAndFlush(tenant);

        ResultActions result = mockMvc.perform(patch("/api/v1/admin/tenants/{id}/deactivate", tenant.getId())
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isConflict());
        Tenant updatedTenant = tenantRepository.findById(tenant.getId()).orElseThrow();
        assertThat(updatedTenant.getStatus()).isEqualTo(TenantStatus.INACTIVE);
    }

    @Test
    void shouldActivateTenantSuccessfully() throws Exception {
        Tenant tenant = createAndSaveTenant("Tenant Name", "tenant-name");
        tenant.setStatus(TenantStatus.INACTIVE);
        tenantRepository.saveAndFlush(tenant);

        ResultActions result = mockMvc.perform(patch("/api/v1/admin/tenants/{id}/activate", tenant.getId())
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isNoContent());
        Tenant updatedTenant = tenantRepository.findById(tenant.getId()).orElseThrow();
        assertThat(updatedTenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    void shouldRejectActivateWhenTenantAlreadyActive() throws Exception {
        Tenant tenant = createAndSaveTenant("Tenant Name", "tenant-name");

        ResultActions result = mockMvc.perform(patch("/api/v1/admin/tenants/{id}/activate", tenant.getId())
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isConflict());
        Tenant updatedTenant = tenantRepository.findById(tenant.getId()).orElseThrow();
        assertThat(updatedTenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    void shouldUpdateTenantSuccessfully() throws Exception {
        Tenant tenant = createAndSaveTenant("Tenant Name", "tenant-name");
        UpdateTenantRequest request = new UpdateTenantRequest(
                "New Tenant Name"
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(put("/api/v1/admin/tenants/{id}", tenant.getId())
                .with(user("test-user").roles("SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenant.getId().toString()))
                .andExpect(jsonPath("$.name").value(request.companyName()))
                .andExpect(jsonPath("$.status").value(tenant.getStatus().toString()));
        Tenant updatedTenant = tenantRepository.findById(tenant.getId()).orElseThrow();
        assertThat(updatedTenant.getName()).isEqualTo(request.companyName());
    }

    @Test
    void shouldRejectUpdateTenantWhenCompanyNameIsTheSame() throws Exception {
        Tenant tenant = createAndSaveTenant("Tenant Name", "tenant-name");
        UpdateTenantRequest request = new UpdateTenantRequest(
                "Tenant Name"
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(put("/api/v1/admin/tenants/{id}", tenant.getId())
                .with(user("test-user").roles("SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        result.andExpect(status().isBadRequest());
    }

    private Tenant createAndSaveTenant(String tenantName, String slug) {
        Tenant tenant = Tenant.builder()
                .name(tenantName)
                .slug(slug + UUID.randomUUID())
                .status(TenantStatus.ACTIVE)
                .build();
        return tenantRepository.saveAndFlush(tenant);
    }
}