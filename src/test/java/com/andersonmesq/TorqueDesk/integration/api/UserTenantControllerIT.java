package com.andersonmesq.TorqueDesk.integration.api;

import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
import com.andersonmesq.TorqueDesk.usertenant.dto.CreateUserTenantRequest;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import com.andersonmesq.TorqueDesk.usertenant.role.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class UserTenantControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private UserTenantRepository userTenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void cleanUp() {
        userTenantRepository.deleteAll();
        tenantRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUserTenantAsSuperAdmin() throws Exception {
        User user = createUser();
        Tenant tenant = createTenant();
        CreateUserTenantRequest request = new CreateUserTenantRequest(user.getId(), tenant.getId(), Role.MECHANIC);

        ResultActions result = mockMvc.perform(post("/api/v1/user-tenants")
                .with(user("admin").roles("SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(user.getId().toString()))
                .andExpect(jsonPath("$.tenantId").value(tenant.getId().toString()))
                .andExpect(jsonPath("$.role").value(Role.MECHANIC.name()))
                .andExpect(jsonPath("$.enabled").value(true));
        assertThat(userTenantRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldRejectDuplicateMembership() throws Exception {
        User member = createUser();
        Tenant tenant = createTenant();
        CreateUserTenantRequest request = new CreateUserTenantRequest(member.getId(), tenant.getId(), Role.ATTENDANT);
        userTenantRepository.saveAndFlush(UserTenant.builder()
                .user(member)
                .tenant(tenant)
                .role(Role.ATTENDANT)
                .enabled(true)
                .build());

        mockMvc.perform(post("/api/v1/user-tenants")
                        .with(user("admin").roles("SUPER_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
        assertThat(userTenantRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldRejectMembershipCreationWhenUserIsNotSuperAdmin() throws Exception {
        Tenant tenant = createTenant();
        CreateUserTenantRequest request = new CreateUserTenantRequest(
                UUID.randomUUID(), tenant.getId(), Role.MECHANIC);

        mockMvc.perform(post("/api/v1/user-tenants")
                        .with(user("member").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        assertThat(userTenantRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectMembershipCreationWhenUnauthenticated() throws Exception {
        Tenant tenant = createTenant();
        CreateUserTenantRequest request = new CreateUserTenantRequest(
                UUID.randomUUID(), tenant.getId(), Role.MECHANIC);

        mockMvc.perform(post("/api/v1/user-tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    private User createUser() {
        return userRepository.saveAndFlush(User.builder()
                .systemRole(SystemRole.USER)
                .fullName("User Tenant API")
                .userName("user-tenant-api-" + UUID.randomUUID())
                .email("user-tenant-api-" + UUID.randomUUID() + "@example.com")
                .password("encoded-password")
                .enabled(true)
                .build());
    }

    private Tenant createTenant() {
        return tenantRepository.saveAndFlush(Tenant.builder()
                .name("User Tenant API")
                .slug("user-tenant-api-" + UUID.randomUUID())
                .status(TenantStatus.ACTIVE)
                .build());
    }
}
