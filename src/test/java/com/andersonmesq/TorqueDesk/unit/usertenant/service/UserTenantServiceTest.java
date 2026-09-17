package com.andersonmesq.TorqueDesk.unit.usertenant.service;

import com.andersonmesq.TorqueDesk.tenant.exception.TenantNotFoundException;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.usertenant.dto.CreateUserTenantRequest;
import com.andersonmesq.TorqueDesk.usertenant.dto.UserTenantResponse;
import com.andersonmesq.TorqueDesk.usertenant.exception.UserTenantAlreadyExistException;
import com.andersonmesq.TorqueDesk.usertenant.mapper.UserTenantMapper;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import com.andersonmesq.TorqueDesk.usertenant.role.Role;
import com.andersonmesq.TorqueDesk.usertenant.service.UserTenantService;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserTenantServiceTest {
    @Mock
    UserTenantRepository userTenantRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    TenantRepository tenantRepository;

    @Mock
    UserTenantMapper userTenantMapper;

    @InjectMocks
    UserTenantService userTenantService;

    @Test
    void shouldCreateUserTenantWhenRequestIsValid() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        CreateUserTenantRequest request = new CreateUserTenantRequest(
                userId,
                tenantId,
                Role.MECHANIC
        );
        User user = User.builder()
                .id(request.userId())
                .build();
        Tenant tenant = Tenant.builder()
                .id(request.tenantId())
                .build();
        UserTenantResponse expectedResponse = new UserTenantResponse(
                UUID.randomUUID(),
                userId,
                tenantId,
                Role.MECHANIC,
                true
        );
        when(userTenantRepository.existsByUserIdAndTenantId(userId, tenantId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(userTenantMapper.toResponse(any(UserTenant.class))).thenReturn(expectedResponse);

        UserTenantResponse response = userTenantService.create(request);

        ArgumentCaptor<UserTenant> captor = ArgumentCaptor.forClass(UserTenant.class);
        verify(userTenantRepository).save(captor.capture());
        UserTenant savedUserTenant = captor.getValue();
        assertThat(savedUserTenant.getUser()).isEqualTo(user);
        assertThat(savedUserTenant.getTenant()).isEqualTo(tenant);
        assertThat(savedUserTenant.getRole()).isEqualTo(Role.MECHANIC);
        assertThat(savedUserTenant.getEnabled()).isTrue();
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void shouldThrowUserTenantAlreadyExistExceptionWhenUserAlreadyLinkedToTenant() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        CreateUserTenantRequest request = new CreateUserTenantRequest(
                userId,
                tenantId,
                Role.MECHANIC
        );
        when(userTenantRepository.existsByUserIdAndTenantId(userId, tenantId)).thenReturn(true);

        ThrowableAssert.ThrowingCallable action = () -> userTenantService.create(request);

        assertThatThrownBy(action).isInstanceOf(UserTenantAlreadyExistException.class).hasMessage("User already linked to tenant");
        verify(userRepository, never()).findById(any());
        verify(tenantRepository, never()).findById(any());
        verify(userTenantRepository, never()).save(any());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        CreateUserTenantRequest request = new CreateUserTenantRequest(
                userId,
                tenantId,
                Role.MECHANIC
        );
        when(userTenantRepository.existsByUserIdAndTenantId(userId, tenantId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> userTenantService.create(request);

        assertThatThrownBy(action).isInstanceOf(UserNotFoundException.class).hasMessage("User not found");
        verify(tenantRepository, never()).findById(any());
        verify(userTenantRepository, never()).save(any());
    }

    @Test
    void shouldThrowTenantNotFoundExceptionWhenTenantNotFound() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        CreateUserTenantRequest request = new CreateUserTenantRequest(
                userId,
                tenantId,
                Role.MECHANIC
        );
        User user = User.builder()
                .id(request.userId())
                .build();
        when(userTenantRepository.existsByUserIdAndTenantId(userId, tenantId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> userTenantService.create(request);

        assertThatThrownBy(action).isInstanceOf(TenantNotFoundException.class).hasMessage("Tenant not found");
        verify(userTenantRepository, never()).save(any());
    }
}