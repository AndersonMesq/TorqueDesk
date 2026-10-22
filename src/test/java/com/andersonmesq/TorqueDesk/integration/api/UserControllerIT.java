package com.andersonmesq.TorqueDesk.integration.api;

import com.andersonmesq.TorqueDesk.security.principal.TorqueDeskPrincipal;
import com.andersonmesq.TorqueDesk.user.dto.ChangePasswordRequest;
import com.andersonmesq.TorqueDesk.user.dto.UpdateUserRequest;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class UserControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldFindCurrentUserSuccessfully() throws Exception {
        User currentUser = createUser();

        mockMvc.perform(get("/api/v1/users/me").with(authenticationFor(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(currentUser.getId().toString()))
                .andExpect(jsonPath("$.fullName").value(currentUser.getFullName()))
                .andExpect(jsonPath("$.email").value(currentUser.getEmail()));
    }

    @Test
    void shouldRejectFindCurrentUserWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldUpdateCurrentUserProfileSuccessfully() throws Exception {
        User currentUser = createUser();
        UpdateUserRequest request = new UpdateUserRequest("Updated User");

        mockMvc.perform(put("/api/v1/users/me")
                        .with(authenticationFor(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value(request.fullName()));

        assertThat(userRepository.findById(currentUser.getId()).orElseThrow().getFullName())
                .isEqualTo(request.fullName());
    }

    @Test
    void shouldRejectProfileUpdateWhenRequestIsInvalid() throws Exception {
        User currentUser = createUser();

        mockMvc.perform(put("/api/v1/users/me")
                        .with(authenticationFor(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRequest(""))))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findById(currentUser.getId()).orElseThrow().getFullName())
                .isEqualTo(currentUser.getFullName());
    }

    @Test
    void shouldRejectProfileUpdateWhenUnauthenticated() throws Exception {
        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRequest("Updated User"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldChangePasswordSuccessfully() throws Exception {
        User currentUser = createUser();
        ChangePasswordRequest request = new ChangePasswordRequest("current-password", "new-password");

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .with(authenticationFor(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        User updatedUser = userRepository.findById(currentUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(request.newPassword(), updatedUser.getPassword())).isTrue();
    }

    @Test
    void shouldRejectPasswordChangeWhenCurrentPasswordIsInvalid() throws Exception {
        User currentUser = createUser();
        String existingPassword = currentUser.getPassword();
        ChangePasswordRequest request = new ChangePasswordRequest("wrong-password", "new-password");

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .with(authenticationFor(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findById(currentUser.getId()).orElseThrow().getPassword())
                .isEqualTo(existingPassword);
    }

    @Test
    void shouldRejectPasswordChangeWhenUnauthenticated() throws Exception {
        mockMvc.perform(patch("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest("current-password", "new-password"))))
                .andExpect(status().isForbidden());
    }

    private User createUser() {
        return userRepository.saveAndFlush(User.builder()
                .systemRole(SystemRole.USER)
                .fullName("Current User")
                .userName("current-" + java.util.UUID.randomUUID())
                .email("current-" + java.util.UUID.randomUUID() + "@example.com")
                .password(passwordEncoder.encode("current-password"))
                .enabled(true)
                .build());
    }

    private RequestPostProcessor authenticationFor(User user) {
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.fromUser(user);
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
