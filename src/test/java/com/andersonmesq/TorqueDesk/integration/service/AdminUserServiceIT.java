package com.andersonmesq.TorqueDesk.integration.service;

import com.andersonmesq.TorqueDesk.admin.dto.CreateUserRequest;
import com.andersonmesq.TorqueDesk.admin.exception.UserEmailAlreadyExistException;
import com.andersonmesq.TorqueDesk.admin.exception.UserNameAlreadyExistException;
import com.andersonmesq.TorqueDesk.admin.service.AdminUserService;
import com.andersonmesq.TorqueDesk.user.dto.UpdateUserRequest;
import com.andersonmesq.TorqueDesk.user.dto.UserResponse;
import com.andersonmesq.TorqueDesk.user.exception.UserAlreadyActiveException;
import com.andersonmesq.TorqueDesk.user.exception.UserAlreadyDeactivatedException;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
public class AdminUserServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminUserService adminUserService;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void shouldFindByIdSuccessfully() {
        User user = createAndSaveUser();

        UserResponse response = adminUserService.findById(user.getId());

        assertThat(response).isNotNull().satisfies(result -> {
            assertThat(result.id()).isEqualTo(user.getId());
            assertThat(result.fullName()).isEqualTo(user.getFullName());
            assertThat(result.email()).isEqualTo(user.getEmail());
            assertThat(result.enabled()).isTrue();
        });
    }

    @Test
    void shouldRejectFindByIdWhenUserDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> adminUserService.findById(nonExistentId))
                .isInstanceOf(UserNotFoundException.class).hasMessage("User not found");
    }

    @Test
    void shouldFindByEmailSuccessfully() {
        User user = createAndSaveUser();

        UserResponse response = adminUserService.findByEmail(user.getEmail());

        assertThat(response).isNotNull().satisfies(result -> {
            assertThat(result.id()).isEqualTo(user.getId());
            assertThat(result.fullName()).isEqualTo(user.getFullName());
            assertThat(result.email()).isEqualTo(user.getEmail());
            assertThat(result.enabled()).isTrue();
        });
    }

    @Test
    void shouldRejectFindByEmailWhenEmailDoesNotExist() {
        String nonExistentEmail = "non.existent@email.com";

        assertThatThrownBy(() -> adminUserService.findByEmail(nonExistentEmail))
                .isInstanceOf(UserNotFoundException.class).hasMessage("User not found");
    }

    @Test
    void shouldFindAllSuccessfully() {
        User user1 = createAndSaveUser("John Test", "JohnTest", "john.test@email.com");
        User user2 = createAndSaveUser("Jane Test", "JaneTest", "jane.test@email.com");
        Pageable pageable = PageRequest.of(0, 10);

        Page<UserResponse> response = adminUserService.findAll(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent()).extracting(UserResponse::id)
                .containsExactlyInAnyOrder(user1.getId(), user2.getId());
    }

    @Test
    void shouldCreateUserSuccessfully() {
        CreateUserRequest request = new CreateUserRequest(
                "John Test",
                "JohnTest",
                "john.test@email.com",
                "123456789"
        );

        UserResponse response = adminUserService.create(request);

        assertThat(response).isNotNull();
        User createdUser = userRepository.findById(response.id()).orElseThrow();
        assertThat(createdUser.getSystemRole()).isEqualTo(SystemRole.USER);
        assertThat(createdUser.getFullName()).isEqualTo(request.fullName());
        assertThat(createdUser.getUserName()).isEqualTo(request.userName());
        assertThat(createdUser.getEmail()).isEqualTo(request.email());
        assertThat(passwordEncoder.matches(request.password(), createdUser.getPassword())).isTrue();
        assertThat(createdUser.getEnabled()).isTrue();
    }

    @Test
    void shouldRejectCreateUserWhenEmailAlreadyExists() {
        User existingUser = createAndSaveUser();
        CreateUserRequest request = new CreateUserRequest(
                "John Test",
                "JohnTest",
                existingUser.getEmail(),
                "123456789"
        );

        assertThatThrownBy(() -> adminUserService.create(request))
                .isInstanceOf(UserEmailAlreadyExistException.class).hasMessage("Email already exist");
    }

    @Test
    void shouldRejectCreateUserWhenUserNameAlreadyExists() {
        User existingUser = createAndSaveUser("John Test", "JohnTest", "john.test@email.com");
        CreateUserRequest request = new CreateUserRequest(
                "Jane Test",
                existingUser.getUserName(),
                "jane.test@email.com",
                "123456789"
        );

        assertThatThrownBy(() -> adminUserService.create(request))
                .isInstanceOf(UserNameAlreadyExistException.class).hasMessage("User already exist");
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        User user = createAndSaveUser();
        UpdateUserRequest request = new UpdateUserRequest(
                "New fullName"
        );

        UserResponse response = adminUserService.update(user.getId(), request);

        assertThat(response).isNotNull().satisfies(result -> {
            assertThat(result.id()).isEqualTo(user.getId());
            assertThat(result.fullName()).isNotEqualTo(user.getFullName());
            assertThat(result.email()).isEqualTo(user.getEmail());
            assertThat(result.enabled()).isTrue();
        });
        User updatedUser = userRepository.findById(response.id()).orElseThrow();
        assertThat(updatedUser.getFullName()).isEqualTo(request.fullName());
    }

    @Test
    void shouldDeactivateUserSuccessfully() {
        User user = createAndSaveUser();

        adminUserService.deactivate(user.getId());

        User deactivatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(deactivatedUser.getEnabled()).isFalse();
    }

    @Test
    void shouldRejectDeactivateWhenUserAlreadyDeactivated() {
        User user = createAndSaveUser();
        user.setEnabled(false);
        userRepository.saveAndFlush(user);

        assertThatThrownBy(() -> adminUserService.deactivate(user.getId()))
                .isInstanceOf(UserAlreadyDeactivatedException.class).hasMessage("User already deactivated");
    }

    @Test
    void shouldActivateUserSuccessfully() {
        User user = createAndSaveUser();
        user.setEnabled(false);
        userRepository.saveAndFlush(user);

        adminUserService.activate(user.getId());

        User activatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(activatedUser.getEnabled()).isTrue();
    }

    @Test
    void shouldRejectActivateWhenUserAlreadyActivated() {
        User user = createAndSaveUser();
        userRepository.saveAndFlush(user);

        assertThatThrownBy(() -> adminUserService.activate(user.getId()))
                .isInstanceOf(UserAlreadyActiveException.class).hasMessage("User already active");
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