package com.andersonmesq.TorqueDesk.authentication.service;

import com.andersonmesq.TorqueDesk.authentication.dto.request.LoginRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.request.RefreshRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.response.LoginResponse;
import com.andersonmesq.TorqueDesk.authentication.dto.response.RefreshResponse;
import com.andersonmesq.TorqueDesk.authentication.mapper.AvailableWorkspaceMapper;
import com.andersonmesq.TorqueDesk.security.context.SecurityUtils;
import com.andersonmesq.TorqueDesk.security.exception.UnauthorizedException;
import com.andersonmesq.TorqueDesk.security.jwt.JwtService;
import com.andersonmesq.TorqueDesk.security.principal.TorqueDeskPrincipal;
import com.andersonmesq.TorqueDesk.security.principal.UserPrincipal;
import com.andersonmesq.TorqueDesk.security.principal.WorkspacePrincipal;
import com.andersonmesq.TorqueDesk.tenant.exception.AccessDeniedException;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
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
        when(jwtService.generateIdentityToken(principal)).thenReturn(identityToken);
        List<UserTenant> workspaces = List.of();
        when(userTenantRepository.findAllByUserId(principal.getUserPrincipal().getId())).thenReturn(workspaces);
        when(principal.isSuperAdmin()).thenReturn(true);

        LoginResponse loginResponse = authenticationService.login(request);

        assertThat(loginResponse.identityToken()).isEqualTo(identityToken);
        assertThat(loginResponse.workspaceToken()).isNull();
        assertThat(loginResponse.workspaces()).isNull();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateIdentityToken(principal);
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
        when(jwtService.generateIdentityToken(principal)).thenReturn(identityToken);
        List<UserTenant> workspaces = List.of(workspace);
        when(userTenantRepository.findAllByUserId(principal.getUserPrincipal().getId())).thenReturn(workspaces);
        String workspaceToken = "workspace-token";
        when(jwtService.generateWorkspaceToken(workspace)).thenReturn(workspaceToken);

        LoginResponse loginResponse = authenticationService.login(request);

        assertThat(loginResponse.identityToken()).isEqualTo(identityToken);
        assertThat(loginResponse.workspaceToken()).isEqualTo(workspaceToken);
        assertThat(loginResponse.workspaces()).isNull();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateIdentityToken(principal);
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
        UserTenant workspace1 = UserTenant.builder()
                .id(UUID.randomUUID())
                .build();
        UserTenant workspace2 = UserTenant.builder()
                .id(UUID.randomUUID())
                .build();
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.fromUser(userPrincipal);
        String identityToken = "identity-token";
        when(jwtService.generateIdentityToken(principal)).thenReturn(identityToken);
        List<UserTenant> workspaces = List.of(workspace1, workspace2);
        when(userTenantRepository.findAllByUserId(principal.getUserPrincipal().getId())).thenReturn(workspaces);

        LoginResponse loginResponse = authenticationService.login(request);

        assertThat(loginResponse.identityToken()).isEqualTo(identityToken);
        assertThat(loginResponse.workspaceToken()).isNull();
        assertThat(loginResponse.workspaces()).isEqualTo(workspaces);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateIdentityToken(principal);
        verify(userTenantRepository).findAllByUserId(principal.getUserPrincipal().getId());
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
    void shouldThrowUserTenantNotFoundExceptionWhenUserTryLoginHasNoWorkspace()  {
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
        when(jwtService.generateIdentityToken(principal)).thenReturn(identityToken);
        List<UserTenant> workspaces = List.of();
        when(userTenantRepository.findAllByUserId(principal.getUserPrincipal().getId())).thenReturn(workspaces);
        when(principal.isSuperAdmin()).thenReturn(false);

        ThrowableAssert.ThrowingCallable action = () -> authenticationService.login(request);

        assertThatThrownBy(action).isInstanceOf(UserTenantNotFoundException.class).hasMessage("No workspace available");
    }

    @Test
    void testeDoSelectWorkspace(){}

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
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.fromUser(user);
        String identityToken = "identity-token";
        when(jwtService.generateIdentityToken(principal)).thenReturn(identityToken);

        RefreshResponse refreshResponse = authenticationService.refresh(request);

        assertThat(refreshResponse.identityToken()).isEqualTo(identityToken);
        verify(jwtService).validateRefreshToken(request.refreshToken());
        verify(jwtService).extractUserId(request.refreshToken());
        verify(userRepository).findById(userId);
        verify(jwtService).generateIdentityToken(principal);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenRefreshTokenIsInvalid() {
        RefreshRequest request = new RefreshRequest(
                "invalid-refresh-token"
        );
        when(jwtService.validateRefreshToken(request.refreshToken())).thenReturn(false);

        ThrowableAssert.ThrowingCallable action = () -> authenticationService.refresh(request);

        assertThatThrownBy(action).isInstanceOf(AccessDeniedException.class).hasMessage("Invalid refresh token");
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
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserIsDisabled(){
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

        assertThatThrownBy(action).isInstanceOf(UnauthorizedException.class).hasMessage("User is not enabled");
    }
}