package com.andersonmesq.TorqueDesk.unit.authentication.service;

import com.andersonmesq.TorqueDesk.authentication.dto.request.LoginRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.request.RefreshRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.request.WorkspaceSelectionRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.response.AvailableWorkspaceResponse;
import com.andersonmesq.TorqueDesk.authentication.dto.response.LoginResponse;
import com.andersonmesq.TorqueDesk.authentication.dto.response.RefreshResponse;
import com.andersonmesq.TorqueDesk.authentication.dto.response.WorkspaceSelectionResponse;
import com.andersonmesq.TorqueDesk.authentication.mapper.AvailableWorkspaceMapper;
import com.andersonmesq.TorqueDesk.authentication.service.AuthenticationService;
import com.andersonmesq.TorqueDesk.security.context.SecurityUtils;
import com.andersonmesq.TorqueDesk.security.exception.UnauthorizedException;
import com.andersonmesq.TorqueDesk.security.jwt.JwtService;
import com.andersonmesq.TorqueDesk.security.principal.TorqueDeskPrincipal;
import com.andersonmesq.TorqueDesk.security.principal.UserPrincipal;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.exception.AccessDeniedException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyDeactivatedException;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
import com.andersonmesq.TorqueDesk.usertenant.exception.UserTenantDisabledException;
import com.andersonmesq.TorqueDesk.usertenant.exception.UserTenantNotFoundException;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import com.andersonmesq.TorqueDesk.usertenant.role.Role;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    UserTenantRepository userTenantRepository;

    @Mock
    JwtService jwtService;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    SecurityUtils securityUtils;

    @Mock
    AvailableWorkspaceMapper availableWorkspaceMapper;

    @InjectMocks
    AuthenticationService authenticationService;

    @Test
    void shouldReturnIdentityTokenOnlyWhenSuperAdminHasNoWorkspace() {
        UUID userPrincipalId = UUID.randomUUID();
        LoginRequest request = new LoginRequest(
                "admin@test.com",
                "12345678"
        );
        UserPrincipal userPrincipal = new UserPrincipal(
                userPrincipalId,
                "admin@test.com",
                "12345678",
                SystemRole.SUPER_ADMIN
        );
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.fromUser(userPrincipal);
        String identityToken = "identity-token";
        when(jwtService.generateIdentityToken(any(TorqueDeskPrincipal.class))).thenReturn(identityToken);
        List<UserTenant> workspaces = List.of();
        when(userTenantRepository.findAllByUserId(principal.getUserPrincipal().getId())).thenReturn(workspaces);

        LoginResponse loginResponse = authenticationService.login(request);

        assertThat(loginResponse.identityToken()).isEqualTo(identityToken);
        assertThat(loginResponse.workspaceToken()).isNull();
        assertThat(loginResponse.workspaces()).isNull();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateIdentityToken(any(TorqueDeskPrincipal.class));
        verify(userTenantRepository).findAllByUserId(principal.getUserPrincipal().getId());
    }

    @Test
    void shouldReturnIdentityTokenAndWorkspaceTokenWhenUserHasOneWorkspace() {
        UUID userPrincipalId = UUID.randomUUID();
        LoginRequest request = new LoginRequest(
                "userTest",
                "12345678"
        );
        UserPrincipal userPrincipal = new UserPrincipal(
                userPrincipalId,
                "userTest",
                "12345678",
                SystemRole.USER
        );
        UserTenant workspace = UserTenant.builder()
                .id(UUID.randomUUID())
                .build();
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.fromUser(userPrincipal);
        String identityToken = "identity-token";
        when(jwtService.generateIdentityToken(any(TorqueDeskPrincipal.class))).thenReturn(identityToken);
        List<UserTenant> workspaces = List.of(workspace);
        when(userTenantRepository.findAllByUserId(principal.getUserPrincipal().getId())).thenReturn(workspaces);
        String workspaceToken = "workspace-token";
        when(jwtService.generateWorkspaceToken(workspace)).thenReturn(workspaceToken);

        LoginResponse loginResponse = authenticationService.login(request);

        assertThat(loginResponse.identityToken()).isEqualTo(identityToken);
        assertThat(loginResponse.workspaceToken()).isEqualTo(workspaceToken);
        assertThat(loginResponse.workspaces()).isNull();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateIdentityToken(any(TorqueDeskPrincipal.class));
        verify(jwtService).generateWorkspaceToken(workspace);
        verify(userTenantRepository).findAllByUserId(principal.getUserPrincipal().getId());
    }

    @Test
    void shouldReturnIdentityTokenAndWorkspacesListWhenUserHasManyWorkspaces() {
        UUID userPrincipalId = UUID.randomUUID();
        LoginRequest request = new LoginRequest(
                "userTest",
                "12345678"
        );
        UserPrincipal userPrincipal = new UserPrincipal(
                userPrincipalId,
                "userTest",
                "12345678",
                SystemRole.USER
        );
        Tenant tenant1 = Tenant.builder()
                .id(UUID.randomUUID())
                .name("Tenant test1")
                .build();
        Tenant tenant2 = Tenant.builder()
                .id(UUID.randomUUID())
                .name("Tenant test2")
                .build();
        UserTenant workspace1 = UserTenant.builder()
                .id(UUID.randomUUID())
                .tenant(tenant1)
                .role(Role.OWNER)
                .enabled(true)
                .build();
        UserTenant workspace2 = UserTenant.builder()
                .id(UUID.randomUUID())
                .tenant(tenant2)
                .role(Role.OWNER)
                .enabled(true)
                .build();
        AvailableWorkspaceResponse response1 = new AvailableWorkspaceResponse(
                workspace1.getId(),
                workspace1.getTenant().getId(),
                workspace1.getTenant().getName(),
                workspace1.getRole()
        );
        AvailableWorkspaceResponse response2 = new AvailableWorkspaceResponse(
                workspace2.getId(),
                workspace2.getTenant().getId(),
                workspace2.getTenant().getName(),
                workspace2.getRole()
        );
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        String identityToken = "identity-token";
        when(jwtService.generateIdentityToken(any(TorqueDeskPrincipal.class))).thenReturn(identityToken);
        List<UserTenant> workspaces = List.of(workspace1, workspace2);
        when(userTenantRepository.findAllByUserId(userPrincipalId)).thenReturn(workspaces);
        when(availableWorkspaceMapper.toResponse(workspace1)).thenReturn(response1);
        when(availableWorkspaceMapper.toResponse(workspace2)).thenReturn(response2);

        LoginResponse loginResponse = authenticationService.login(request);

        assertThat(loginResponse.identityToken()).isEqualTo(identityToken);
        assertThat(loginResponse.workspaceToken()).isNull();
        assertThat(loginResponse.workspaces()).isEqualTo(List.of(response1, response2));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateIdentityToken(any(TorqueDeskPrincipal.class));
        verify(userTenantRepository).findAllByUserId(userPrincipalId);
    }

    @Test
    void shouldRejectWhenLoginCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest(
                "userTest",
                "12345678"
        );
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Bad credentials"));

        ThrowableAssert.ThrowingCallable action = () -> authenticationService.login(request);

        assertThatThrownBy(action).isInstanceOf(BadCredentialsException.class).hasMessage("Bad credentials");
        verify(jwtService, never()).generateIdentityToken(any(TorqueDeskPrincipal.class));
    }

    @Test
    void shouldThrowUserTenantNotFoundExceptionWhenUserTryLoginHasNoWorkspace() {
        UUID userPrincipalId = UUID.randomUUID();
        LoginRequest request = new LoginRequest(
                "userTest",
                "12345678"
        );
        UserPrincipal userPrincipal = new UserPrincipal(
                userPrincipalId,
                "userTest",
                "12345678",
                SystemRole.USER
        );
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.fromUser(userPrincipal);
        String identityToken = "identity-token";
        when(jwtService.generateIdentityToken(any(TorqueDeskPrincipal.class))).thenReturn(identityToken);
        List<UserTenant> workspaces = List.of();
        when(userTenantRepository.findAllByUserId(principal.getUserPrincipal().getId())).thenReturn(workspaces);

        ThrowableAssert.ThrowingCallable action = () -> authenticationService.login(request);

        assertThatThrownBy(action).isInstanceOf(UserTenantNotFoundException.class).hasMessage("No workspace available");
    }

    @Test
    void shouldReturnWorkspaceSelectionResponseWhenWorkspaceIsValid() {
        UUID userTenantId = UUID.randomUUID();
        UUID userPrincipalId = UUID.randomUUID();
        WorkspaceSelectionRequest request = new WorkspaceSelectionRequest(
                userTenantId
        );
        UserPrincipal userPrincipal = new UserPrincipal(
                userPrincipalId,
                "userTest",
                "12345678",
                SystemRole.USER
        );
        User user = User.builder()
                .id(userPrincipalId)
                .build();
        Tenant tenant = Tenant.builder()
                .id(UUID.randomUUID())
                .name("Tenant Test")
                .status(TenantStatus.ACTIVE)
                .build();
        UserTenant workspace = UserTenant.builder()
                .id(request.userTenantId())
                .user(user)
                .tenant(tenant)
                .role(Role.OWNER)
                .enabled(true)
                .build();
        when(userTenantRepository.findById(request.userTenantId())).thenReturn(Optional.of(workspace));
        String workspaceToken = "workspace-token";
        when(jwtService.generateWorkspaceToken(workspace)).thenReturn(workspaceToken);
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.fromUser(userPrincipal);
        when(securityUtils.getPrincipal()).thenReturn(principal);


        WorkspaceSelectionResponse workspaceSelectionResponse = authenticationService.selectWorkspace(request);

        assertThat(workspaceSelectionResponse.workspaceToken()).isEqualTo(workspaceToken);
        assertThat(workspaceSelectionResponse.tenantId()).isEqualTo(tenant.getId());
        assertThat(workspaceSelectionResponse.tenantName()).isEqualTo(tenant.getName());
        assertThat(workspaceSelectionResponse.role()).isEqualTo(workspace.getRole());
        verify(securityUtils).getPrincipal();
        verify(jwtService).generateWorkspaceToken(workspace);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenAuthenticationIsNull() {
        UUID userTenantId = UUID.randomUUID();
        WorkspaceSelectionRequest request = new WorkspaceSelectionRequest(
                userTenantId
        );
        when(securityUtils.getPrincipal()).thenThrow(new UnauthorizedException("User not authenticated"));

        ThrowableAssert.ThrowingCallable action = () -> authenticationService.selectWorkspace(request);

        assertThatThrownBy(action).isInstanceOf(UnauthorizedException.class).hasMessage("User not authenticated");
        verify(jwtService, never()).generateWorkspaceToken(any(UserTenant.class));
    }

    @Test
    void shouldThrowUserTenantNotFoundExceptionWhenUserTenantIdNoExist() {
        UUID userTenantId = UUID.randomUUID();
        WorkspaceSelectionRequest request = new WorkspaceSelectionRequest(
                userTenantId
        );
        when(userTenantRepository.findById(request.userTenantId())).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> authenticationService.selectWorkspace(request);

        assertThatThrownBy(action).isInstanceOf(UserTenantNotFoundException.class).hasMessage("Workspace not found");
        verify(jwtService, never()).generateWorkspaceToken(any(UserTenant.class));
    }

    @Test
    void shouldThrowUserTenantDisabledExceptionWhenUserTenantIsDisabled() {
        UUID userTenantId = UUID.randomUUID();
        UUID userPrincipalId = UUID.randomUUID();
        WorkspaceSelectionRequest request = new WorkspaceSelectionRequest(
                userTenantId
        );
        UserPrincipal userPrincipal = new UserPrincipal(
                userPrincipalId,
                "userTest",
                "12345678",
                SystemRole.USER
        );
        User user = User.builder()
                .id(userPrincipalId)
                .build();
        Tenant tenant = Tenant.builder()
                .id(UUID.randomUUID())
                .name("Tenant Test")
                .status(TenantStatus.ACTIVE)
                .build();
        UserTenant workspace = UserTenant.builder()
                .id(request.userTenantId())
                .user(user)
                .tenant(tenant)
                .role(Role.OWNER)
                .enabled(false)
                .build();
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.fromUser(userPrincipal);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(userTenantRepository.findById(request.userTenantId())).thenReturn(Optional.of(workspace));

        ThrowableAssert.ThrowingCallable action = () -> authenticationService.selectWorkspace(request);

        assertThatThrownBy(action).isInstanceOf(UserTenantDisabledException.class).hasMessage("Workspace is disabled.");
        verify(jwtService, never()).generateWorkspaceToken(any(UserTenant.class));
    }

    @Test
    void shouldThrowTenantAlreadyDeactivatedExceptionWhenTenantIsInactive() {
        UUID userTenantId = UUID.randomUUID();
        UUID userPrincipalId = UUID.randomUUID();
        WorkspaceSelectionRequest request = new WorkspaceSelectionRequest(
                userTenantId
        );
        UserPrincipal userPrincipal = new UserPrincipal(
                userPrincipalId,
                "userTest",
                "12345678",
                SystemRole.USER
        );
        User user = User.builder()
                .id(userPrincipalId)
                .build();
        Tenant tenant = Tenant.builder()
                .id(UUID.randomUUID())
                .name("Tenant Test")
                .status(TenantStatus.INACTIVE)
                .build();
        UserTenant workspace = UserTenant.builder()
                .id(request.userTenantId())
                .user(user)
                .tenant(tenant)
                .role(Role.OWNER)
                .enabled(true)
                .build();
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.fromUser(userPrincipal);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(userTenantRepository.findById(request.userTenantId())).thenReturn(Optional.of(workspace));

        ThrowableAssert.ThrowingCallable action = () -> authenticationService.selectWorkspace(request);

        assertThatThrownBy(action).isInstanceOf(TenantAlreadyDeactivatedException.class).hasMessage("Tenant is disabled");
        verify(jwtService, never()).generateWorkspaceToken(any(UserTenant.class));
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUserIdsIsIncompatible() {
        UUID userTenantId = UUID.randomUUID();
        UUID userPrincipalId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WorkspaceSelectionRequest request = new WorkspaceSelectionRequest(
                userTenantId
        );
        UserPrincipal userPrincipal = new UserPrincipal(
                userPrincipalId,
                "userTest",
                "12345678",
                SystemRole.USER
        );
        User user = User.builder()
                .id(userId)
                .build();
        Tenant tenant = Tenant.builder()
                .id(UUID.randomUUID())
                .name("Tenant Test")
                .status(TenantStatus.ACTIVE)
                .build();
        UserTenant workspace = UserTenant.builder()
                .id(request.userTenantId())
                .user(user)
                .tenant(tenant)
                .role(Role.OWNER)
                .enabled(true)
                .build();
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.fromUser(userPrincipal);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(userTenantRepository.findById(request.userTenantId())).thenReturn(Optional.of(workspace));

        ThrowableAssert.ThrowingCallable action = () -> authenticationService.selectWorkspace(request);

        assertThatThrownBy(action).isInstanceOf(AccessDeniedException.class).hasMessage("Workspace does not belong to authenticated user.");
        verify(jwtService, never()).generateWorkspaceToken(any(UserTenant.class));
    }

    @Test
    void shouldReturnRefreshResponseWhenRefreshTokenIsValid() {
        UUID userId = UUID.randomUUID();
        RefreshRequest request = new RefreshRequest(
                "refresh-token"
        );
        User user = User.builder()
                .id(userId)
                .email("user@test.com")
                .password("12345678")
                .systemRole(SystemRole.USER)
                .enabled(true)
                .build();
        when(jwtService.validateRefreshToken(request.refreshToken())).thenReturn(true);
        when(jwtService.extractUserId(request.refreshToken())).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        String identityToken = "identity-token";
        when(jwtService.generateIdentityToken(any(TorqueDeskPrincipal.class))).thenReturn(identityToken);

        RefreshResponse refreshResponse = authenticationService.refresh(request);

        assertThat(refreshResponse.identityToken()).isEqualTo(identityToken);
        verify(jwtService).validateRefreshToken(request.refreshToken());
        verify(jwtService).extractUserId(request.refreshToken());
        verify(userRepository).findById(userId);
        verify(jwtService).generateIdentityToken(any(TorqueDeskPrincipal.class));
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenRefreshTokenIsInvalid() {
        RefreshRequest request = new RefreshRequest(
                "invalid-refresh-token"
        );
        when(jwtService.validateRefreshToken(request.refreshToken())).thenReturn(false);

        ThrowableAssert.ThrowingCallable action = () -> authenticationService.refresh(request);

        assertThatThrownBy(action).isInstanceOf(AccessDeniedException.class).hasMessage("Invalid refresh token");
        verify(jwtService, never()).extractUserId(any(String.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenIdNotExistInRefreshToken() {
        UUID userId = UUID.randomUUID();
        RefreshRequest request = new RefreshRequest(
                "refresh-token"
        );
        when(jwtService.validateRefreshToken(request.refreshToken())).thenReturn(true);
        when(jwtService.extractUserId(request.refreshToken())).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> authenticationService.refresh(request);

        assertThatThrownBy(action).isInstanceOf(UserNotFoundException.class).hasMessage("User not found");
        verify(jwtService, never()).generateIdentityToken(any(TorqueDeskPrincipal.class));
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserIsDisabled() {
        UUID userId = UUID.randomUUID();
        RefreshRequest request = new RefreshRequest(
                "refresh-token"
        );
        User user = User.builder()
                .id(userId)
                .email("user@test.com")
                .password("12345678")
                .systemRole(SystemRole.USER)
                .enabled(false)
                .build();
        when(jwtService.validateRefreshToken(request.refreshToken())).thenReturn(true);
        when(jwtService.extractUserId(request.refreshToken())).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ThrowableAssert.ThrowingCallable action = () -> authenticationService.refresh(request);

        assertThatThrownBy(action).isInstanceOf(UnauthorizedException.class).hasMessage("User is disabled.");
        verify(jwtService, never()).generateIdentityToken(any(TorqueDeskPrincipal.class));
    }
}