package com.andersonmesq.TorqueDesk.integration.api;

import com.andersonmesq.TorqueDesk.admin.dto.CreateUserRequest;
import com.andersonmesq.TorqueDesk.tenant.dto.CreateTenantRequest;
import com.andersonmesq.TorqueDesk.user.dto.UpdateUserRequest;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class AdminUserControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUserSuccessfully() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "John Test",
                "JohnTest",
                "john.test@email.com",
                "123456789"
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/api/v1/admin/users")
                .with(user("test-user").roles("SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.fullName").value(request.fullName()))
                .andExpect(jsonPath("$.email").value(request.email()))
                .andExpect(jsonPath("$.enabled").value(true));
        assertThat(userRepository.findAll()).hasSize(1).first().satisfies(user -> {
            assertThat(user.getId()).isNotNull();
            assertThat(user.getSystemRole()).isEqualTo(SystemRole.USER);
            assertThat(user.getFullName()).isEqualTo(request.fullName());
            assertThat(user.getUserName()).isEqualTo(request.userName());
            assertThat(user.getEmail()).isEqualTo(request.email());
            assertThat(user.getEnabled()).isEqualTo(true);
        });
    }

    @Test
    void shouldCreateUserWithoutEmail() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "John Test",
                "JohnTest",
                "",
                "123456789"
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/api/v1/admin/users")
                .with(user("test-user").roles("SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.fullName").value(request.fullName()))
                .andExpect(jsonPath("$.email").isEmpty())
                .andExpect(jsonPath("$.enabled").value(true));
        assertThat(userRepository.findAll()).hasSize(1).first().satisfies(user -> {
            assertThat(user.getId()).isNotNull();
            assertThat(user.getSystemRole()).isEqualTo(SystemRole.USER);
            assertThat(user.getFullName()).isEqualTo(request.fullName());
            assertThat(user.getUserName()).isEqualTo(request.userName());
            assertThat(user.getEmail()).isEmpty();
            assertThat(user.getEnabled()).isEqualTo(true);
        });
    }

    @Test
    void shouldCreateUserWithoutUserName() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "John Test",
                "",
                "john.test@email.com",
                "123456789"
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/api/v1/admin/users")
                .with(user("test-user").roles("SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.fullName").value(request.fullName()))
                .andExpect(jsonPath("$.email").value(request.email()))
                .andExpect(jsonPath("$.enabled").value(true));
        assertThat(userRepository.findAll()).hasSize(1).first().satisfies(user -> {
            assertThat(user.getId()).isNotNull();
            assertThat(user.getSystemRole()).isEqualTo(SystemRole.USER);
            assertThat(user.getFullName()).isEqualTo(request.fullName());
            assertThat(user.getUserName()).isEmpty();
            assertThat(user.getEmail()).isEqualTo(request.email());
            assertThat(user.getEnabled()).isEqualTo(true);
        });
    }

    @Test
    void shouldRejectCreateUserWhenUserIsNotAuthenticated() throws Exception {
        CreateTenantRequest request = new CreateTenantRequest(
                "Test Tenant",
                "John test",
                "john.test@email.com",
                "123456789"
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/api/v1/admin/tenants")
                .with(user("test-user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        result.andExpect(status().isForbidden());
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectCreateUserWhenRequestIsInvalid() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "",
                "JohnTest",
                "john.test@email.com",
                "123456789"
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/api/v1/admin/users")
                .with(user("test-user").roles("SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        result.andExpect(status().isBadRequest());
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    void shouldFindByIdSuccessfully() throws Exception {
        User user = createAndSaveUser();

        ResultActions result = mockMvc.perform(get("/api/v1/admin/users/{id}", user.getId())
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.fullName").value(user.getFullName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.enabled").value(user.getEnabled()));
    }

    @Test
    void shouldRejectFindByIdWhenIdDoesNotExist() throws Exception {
        UUID inexistentId = UUID.randomUUID();

        ResultActions result = mockMvc.perform(get("/api/v1/admin/users/{id}", inexistentId)
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isNotFound());
    }

    @Test
    void shouldFindByEmailSuccessfully() throws Exception {
        User user = createAndSaveUser();

        ResultActions result = mockMvc.perform(get("/api/v1/admin/users/email/{email}", user.getEmail())
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.fullName").value(user.getFullName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.enabled").value(user.getEnabled()));
    }

    @Test
    void shouldRejectFindByEmailWhenEmailDoesNotExists() throws Exception {
        String inexistentEmail = "inexistent@email.com";

        ResultActions result = mockMvc.perform(get("/api/v1/admin/users/email/{email}", inexistentEmail)
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isNotFound());
    }

    @Test
    void shouldFindAllSuccessfully() throws Exception {
        User user1 = createAndSaveUser();
        User user2 = createAndSaveUser("Jane Test", "JaneTest", "jane.test@email.com");

        ResultActions result = mockMvc.perform(get("/api/v1/admin/users")
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andExpect(jsonPath("$.content[0].fullName").value(user1.getFullName()))
                .andExpect(jsonPath("$.content[0].email").value(user1.getEmail()))
                .andExpect(jsonPath("$.content[0].enabled").value(user1.getEnabled()))
                .andExpect(jsonPath("$.content[1].id").isNotEmpty())
                .andExpect(jsonPath("$.content[1].fullName").value(user2.getFullName()))
                .andExpect(jsonPath("$.content[1].email").value(user2.getEmail()))
                .andExpect(jsonPath("$.content[1].enabled").value(user2.getEnabled()));
    }

    @Test
    void shouldUpdateSuccessfully() throws Exception {
        User user = createAndSaveUser();
        UpdateUserRequest request = new UpdateUserRequest(
                "New fullName"
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(put("/api/v1/admin/users/{id}", user.getId())
                .with(user("test-user").roles("SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value(request.fullName()));
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getFullName()).isEqualTo(request.fullName());
    }

    @Test
    void shouldRejectUpdateWhenRequestIsInvalid() throws Exception {
        User user = createAndSaveUser();
        UpdateUserRequest request = new UpdateUserRequest(
                ""
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(put("/api/v1/admin/users/{id}", user.getId())
                .with(user("test-user").roles("SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        result.andExpect(status().isBadRequest());
        User unupdatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(unupdatedUser.getFullName()).isNotEqualTo(request.fullName());
    }

    @Test
    void shouldActivateUserSuccessfully() throws Exception {
        User user = createAndSaveUser();
        user.setEnabled(false);
        userRepository.saveAndFlush(user);

        ResultActions result = mockMvc.perform(patch("/api/v1/admin/users/{id}/activate", user.getId())
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isNoContent());
        User userDeactivated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(userDeactivated.getEnabled()).isTrue();
    }

    @Test
    void shouldRejectActivateWhenUserAlreadyActive() throws Exception {
        User user = createAndSaveUser();

        ResultActions result = mockMvc.perform(patch("/api/v1/admin/users/{id}/activate", user.getId())
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isConflict());
    }

    @Test
    void shouldDeactivateUserSuccessfully() throws Exception {
        User user = createAndSaveUser();

        ResultActions result = mockMvc.perform(patch("/api/v1/admin/users/{id}/deactivate", user.getId())
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isNoContent());
        User userDeactivated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(userDeactivated.getEnabled()).isFalse();
    }

    @Test
    void shouldRejectDeactivateWhenUserAlreadyDeactivated() throws Exception {
        User user = createAndSaveUser();
        user.setEnabled(false);
        userRepository.saveAndFlush(user);

        ResultActions result = mockMvc.perform(patch("/api/v1/admin/users/{id}/deactivate", user.getId())
                .with(user("test-user").roles("SUPER_ADMIN"))
        );

        result.andExpect(status().isConflict());
    }

    private User createAndSaveUser() {
        User user = User.builder()
                .systemRole(SystemRole.USER)
                .fullName("John Test")
                .userName("JohnTest")
                .email("john.test@email.com")
                .password("123456789")
                .enabled(true)
                .build();
        return userRepository.saveAndFlush(user);
    }

    private User createAndSaveUser(String fullName, String userName, String email) {
        User user = User.builder()
                .systemRole(SystemRole.USER)
                .fullName(fullName)
                .userName(userName)
                .email(email)
                .password("123456789")
                .enabled(true)
                .build();
        return userRepository.saveAndFlush(user);
    }
}