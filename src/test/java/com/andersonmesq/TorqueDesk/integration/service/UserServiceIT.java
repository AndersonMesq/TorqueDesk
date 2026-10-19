package com.andersonmesq.TorqueDesk.integration.service;

import com.andersonmesq.TorqueDesk.security.principal.TorqueDeskPrincipal;
import com.andersonmesq.TorqueDesk.user.dto.ChangePasswordRequest;
import com.andersonmesq.TorqueDesk.user.dto.UpdateUserRequest;
import com.andersonmesq.TorqueDesk.user.exception.InvalidPasswordException;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.user.service.UserService;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
class UserServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        userRepository.deleteAll();
    }

    @Test
    void shouldFindCurrentUserSuccessfully() {
        User user = createUser();
        authenticateAs(user);

        var response = userService.findMe();

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.fullName()).isEqualTo(user.getFullName());
        assertThat(response.email()).isEqualTo(user.getEmail());
    }

    @Test
    void shouldRejectFindingCurrentUserWhenUserRecordDoesNotExist() {
        User missingUser = User.builder()
                .id(UUID.randomUUID())
                .systemRole(SystemRole.USER)
                .fullName("Missing User")
                .build();
        authenticateAs(missingUser);

        assertThatThrownBy(() -> userService.findMe())
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void shouldUpdateCurrentUserProfileSuccessfully() {
        User user = createUser();
        authenticateAs(user);

        var response = userService.updateProfile(new UpdateUserRequest("Updated Name"));

        assertThat(response.fullName()).isEqualTo("Updated Name");
        assertThat(userRepository.findById(user.getId()).orElseThrow().getFullName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldRejectProfileUpdateWhenCurrentUserDoesNotExist() {
        User missingUser = User.builder()
                .id(UUID.randomUUID())
                .systemRole(SystemRole.USER)
                .fullName("Missing User")
                .build();
        authenticateAs(missingUser);

        assertThatThrownBy(() -> userService.updateProfile(new UpdateUserRequest("Updated Name")))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void shouldChangePasswordWhenCurrentPasswordIsValid() {
        User user = createUser();
        authenticateAs(user);
        ChangePasswordRequest request = new ChangePasswordRequest("current-password", "new-password");

        userService.changePassword(request);

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(request.newPassword(), updatedUser.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(request.currentPassword(), updatedUser.getPassword())).isFalse();
    }

    @Test
    void shouldRejectPasswordChangeWhenCurrentPasswordIsInvalid() {
        User user = createUser();
        authenticateAs(user);
        String existingPassword = user.getPassword();

        assertThatThrownBy(() -> userService.changePassword(
                new ChangePasswordRequest("incorrect-password", "new-password")))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("Current password invalid");

        assertThat(userRepository.findById(user.getId()).orElseThrow().getPassword()).isEqualTo(existingPassword);
    }

    private User createUser() {
        return userRepository.saveAndFlush(User.builder()
                .systemRole(SystemRole.USER)
                .fullName("Current User")
                .userName("current-" + UUID.randomUUID())
                .email("current-" + UUID.randomUUID() + "@example.com")
                .password(passwordEncoder.encode("current-password"))
                .enabled(true)
                .build());
    }

    private void authenticateAs(User user) {
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.fromUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
