package com.andersonmesq.TorqueDesk.admin.service;

import com.andersonmesq.TorqueDesk.admin.dto.CreateUserRequest;
import com.andersonmesq.TorqueDesk.admin.exception.UserEmailAlreadyExistException;
import com.andersonmesq.TorqueDesk.admin.exception.UserNameAlreadyExistException;
import com.andersonmesq.TorqueDesk.admin.mapper.AdminUserMapper;
import com.andersonmesq.TorqueDesk.user.dto.UpdateUserRequest;
import com.andersonmesq.TorqueDesk.user.dto.UserResponse;
import com.andersonmesq.TorqueDesk.user.exception.UserAlreadyActiveException;
import com.andersonmesq.TorqueDesk.user.exception.UserAlreadyDeactivatedException;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminUserServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    AdminUserMapper adminUserMapper;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AdminUserService adminUserService;

    @Test
    void shouldCreateUserWhenRequestIsValid() {
        UUID userId = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest(
                "John Test",
                "johntest",
                "john@test.com",
                "12345678"
        );
        UserResponse expectedResponse = new UserResponse(
                userId,
                "John Test",
                "john@test.com",
                true
        );
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUserName(request.userName())).thenReturn(false);
        when(adminUserMapper.toResponse(any(User.class))).thenReturn(expectedResponse);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");

        adminUserService.create(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getFullName()).isEqualTo(request.fullName());
        assertThat(savedUser.getUserName()).isEqualTo(request.userName());
        assertThat(savedUser.getEmail()).isEqualTo(request.email());
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getEnabled()).isTrue();
    }

    @Test
    void shouldThrowUserEmailAlreadyExistExceptionWhenEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest(
                "John Test",
                "johntest",
                "john@test.com",
                "12345678"
        );
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        ThrowableAssert.ThrowingCallable action = () -> adminUserService.create(request);

        assertThatThrownBy(action).isInstanceOf(UserEmailAlreadyExistException.class).hasMessage("Email already exist");
        verify(userRepository, never()).save(any(User.class));
        verify(adminUserMapper, never()).toResponse(any(User.class));

    }

    @Test
    void shouldThrowUserNameAlreadyExistExceptionWhenUserNameAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest(
                "John Test",
                "johntest",
                "john@test.com",
                "12345678"
        );
        when(userRepository.existsByUserName(request.userName())).thenReturn(true);

        ThrowableAssert.ThrowingCallable action = () -> adminUserService.create(request);

        assertThatThrownBy(action).isInstanceOf(UserNameAlreadyExistException.class).hasMessage("User already exist");
        verify(userRepository, never()).save(any(User.class));
        verify(adminUserMapper, never()).toResponse(any(User.class));
    }

    @Test
    void shouldReturnUserWhenFindByIdRequestIsValid() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .fullName("John Test")
                .userName("johntest")
                .email("john@test.com")
                .enabled(true)
                .build();
        UserResponse expectedResponse = new UserResponse(
                userId,
                "John Test",
                "john@test.com",
                true
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(adminUserMapper.toResponse(any(User.class))).thenReturn(expectedResponse);

        UserResponse userResponse = adminUserService.findById(userId);

        assertThat(userResponse).isEqualTo(expectedResponse);
        verify(adminUserMapper).toResponse(any(User.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserIdNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> adminUserService.findById(userId);

        assertThatThrownBy(action).isInstanceOf(UserNotFoundException.class).hasMessage("User not found");
        verify(adminUserMapper, never()).toResponse(any(User.class));
    }

    @Test
    void shouldReturnUserWhenFindByEmailRequestIsValid() {
        String email = "john@test.com";
        User user = User.builder()
                .fullName("John Test")
                .userName("johntest")
                .email(email)
                .enabled(true)
                .build();
        UserResponse expectedResponse = new UserResponse(
                null,
                "John Test",
                "john@test.com",
                true
        );
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(adminUserMapper.toResponse(any(User.class))).thenReturn(expectedResponse);

        UserResponse userResponse = adminUserService.findByEmail(email);

        assertThat(userResponse).isEqualTo(expectedResponse);
        verify(userRepository).findByEmail(email);
        verify(adminUserMapper).toResponse(any(User.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserEmailNotExist() {
        String email = "john@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> adminUserService.findByEmail(email);

        assertThatThrownBy(action).isInstanceOf(UserNotFoundException.class).hasMessage("User not found");
    }

    @Test
    void shouldUpdateUserWhenRequestIsValid() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .fullName("John Test")
                .userName("johntest")
                .email("john@test.com")
                .enabled(true)
                .build();
        UpdateUserRequest request = new UpdateUserRequest(
                "John Test Updated"
        );
        UserResponse expectedResponse = new UserResponse(
                userId,
                "John Test Updated",
                "john@test.com",
                true
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(adminUserMapper.toResponse(any(User.class))).thenReturn(expectedResponse);

        UserResponse userResponse = adminUserService.update(userId, request);

        assertThat(userResponse).isEqualTo(expectedResponse);
        assertThat(user.getFullName()).isEqualTo(request.fullName());
        verify(userRepository).findById(userId);
        verify(adminUserMapper).toResponse(user);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUpdatingNonExistingUser() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest(
                "John Test Updated"
        );
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> adminUserService.update(userId, request);

        assertThatThrownBy(action).isInstanceOf(UserNotFoundException.class).hasMessage("User not found");
        verify(adminUserMapper, never()).toResponse(any(User.class));
    }

    @Test
    void shouldActivateUserWhenEnableIsFalse() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .enabled(false)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        adminUserService.activate(userId);

        assertThat(user.getEnabled()).isTrue();
    }

    @Test
    void shouldThrowUserAlreadyActiveExceptionWhenEnableIsTrue() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .enabled(true)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ThrowableAssert.ThrowingCallable action = () -> adminUserService.activate(userId);

        assertThatThrownBy(action).isInstanceOf(UserAlreadyActiveException.class).hasMessage("User already active");
    }

    @Test
    void shouldDeactivateUserWhenEnableIsTrue() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .enabled(true)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        adminUserService.deactivate(userId);

        assertThat(user.getEnabled()).isFalse();
    }

    @Test
    void shouldThrowUserAlreadyDeactivatedExceptionWhenEnableIsFalse() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .enabled(false)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ThrowableAssert.ThrowingCallable action = () -> adminUserService.deactivate(userId);

        assertThatThrownBy(action).isInstanceOf(UserAlreadyDeactivatedException.class).hasMessage("User already deactivated");
    }
}