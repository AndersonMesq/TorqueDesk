package com.andersonmesq.TorqueDesk.user.service;

import com.andersonmesq.TorqueDesk.security.context.SecurityUtils;
import com.andersonmesq.TorqueDesk.security.principal.TorqueDeskPrincipal;
import com.andersonmesq.TorqueDesk.user.dto.ChangePasswordRequest;
import com.andersonmesq.TorqueDesk.user.dto.UpdateUserRequest;
import com.andersonmesq.TorqueDesk.user.dto.UserResponse;
import com.andersonmesq.TorqueDesk.user.exception.InvalidPasswordException;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.user.mapper.UserMapper;
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
public class UserServiceTest {
    @Mock
    UserRepository  userRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    SecurityUtils securityUtils;

    @InjectMocks
    UserService userService;

    @Test
    void shouldReturnCurrentUserWhenUserExists() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .fullName("John Test")
                .email("john@test.com")
                .enabled(true)
                .build();
        UserResponse expectedResponse = new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getEnabled()
        );
        TorqueDeskPrincipal principal = mock(TorqueDeskPrincipal.class);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(principal.getUserId()).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.toResponse(any(User.class))).thenReturn(expectedResponse);

        UserResponse response = userService.findMe();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).toResponse(captor.capture());
        User returnedUser = captor.getValue();
        assertThat(returnedUser).isEqualTo(user);
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenCurrentUserDoesNotExist(){
        UUID userId = UUID.randomUUID();
        TorqueDeskPrincipal principal = mock(TorqueDeskPrincipal.class);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(principal.getUserId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(principal.getUserId())).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> userService.findMe();

        assertThatThrownBy(action).isInstanceOf(UserNotFoundException.class).hasMessage("User not found");
        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    void shouldReturnUserUpdatedWhenUpdateRequestIsValid(){
        String userName = "John Test";
        String updatedUserName = "John Updated";
        User user = User.builder()
                .id(UUID.randomUUID())
                .fullName(userName)
                .email("john@test.com")
                .enabled(true)
                .build();
        UpdateUserRequest request = new UpdateUserRequest(
                updatedUserName
        );
        UserResponse expectedResponse = new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getEnabled()
        );
        TorqueDeskPrincipal principal = mock(TorqueDeskPrincipal.class);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(principal.getUserId()).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.toResponse(any(User.class))).thenReturn(expectedResponse);

        userService.updateProfile(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).toResponse(captor.capture());
        User returnedUser = captor.getValue();
        assertThat(returnedUser.getFullName()).isEqualTo(updatedUserName);
    }

    @Test
    void shouldUpdatePasswordWhenChangePasswordRequestIsValid(){
        User user = User.builder()
                .id(UUID.randomUUID())
                .fullName("John Test")
                .email("john@test.com")
                .password("currentPassword")
                .enabled(true)
                .build();
        ChangePasswordRequest request = new ChangePasswordRequest(
                "currentPassword",
                "newPassword"
        );
        TorqueDeskPrincipal principal = mock(TorqueDeskPrincipal.class);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(principal.getUserId()).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        String encodedNewPassword = "encodedNewPassword";
        when(passwordEncoder.encode(request.newPassword())).thenReturn(encodedNewPassword);
        when(passwordEncoder.matches(request.currentPassword(), user.getPassword())).thenReturn(true);

        userService.changePassword(request);

        assertThat(user.getPassword()).isEqualTo(encodedNewPassword);
        verify(passwordEncoder).matches(request.currentPassword(), "currentPassword");
        verify(passwordEncoder).encode(request.newPassword());
    }

    @Test
    void shouldThrowInvalidPasswordExceptionWhenCurrentPasswordDoesNotMatch(){
        User user = User.builder()
                .id(UUID.randomUUID())
                .fullName("John Test")
                .email("john@test.com")
                .password("encodedPassword")
                .enabled(true)
                .build();
        ChangePasswordRequest request = new ChangePasswordRequest(
                "currentPassword",
                "newPassword"
        );
        TorqueDeskPrincipal principal = mock(TorqueDeskPrincipal.class);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(principal.getUserId()).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.currentPassword(), user.getPassword())).thenReturn(false);

        ThrowableAssert.ThrowingCallable action = () -> userService.changePassword(request);

        assertThatThrownBy(action).isInstanceOf(InvalidPasswordException.class).hasMessage("Current password invalid");
        verify(passwordEncoder, never()).encode(anyString());
    }
}