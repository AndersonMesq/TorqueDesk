package com.andersonmesq.TorqueDesk.authentication.service;

import com.andersonmesq.TorqueDesk.authentication.dto.request.LoginRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.request.RefreshRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.request.WorkspaceSelectionRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.response.LoginResponse;
import com.andersonmesq.TorqueDesk.authentication.dto.response.AvailableWorkspaceResponse;
import com.andersonmesq.TorqueDesk.authentication.dto.response.RefreshResponse;
import com.andersonmesq.TorqueDesk.authentication.dto.response.WorkspaceSelectionResponse;
import com.andersonmesq.TorqueDesk.authentication.mapper.AvailableWorkspaceMapper;
import com.andersonmesq.TorqueDesk.security.context.SecurityUtils;
import com.andersonmesq.TorqueDesk.security.exception.UnauthorizedException;
import com.andersonmesq.TorqueDesk.security.jwt.JwtService;
import com.andersonmesq.TorqueDesk.security.principal.TorqueDeskPrincipal;
import com.andersonmesq.TorqueDesk.security.principal.UserPrincipal;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.exception.AccessDeniedException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyDeactivatedException;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.usertenant.exception.UserTenantDisabledException;
import com.andersonmesq.TorqueDesk.usertenant.exception.UserTenantNotFoundException;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserTenantRepository userTenantRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SecurityUtils securityUtils;
    private final AvailableWorkspaceMapper mapper;

    public LoginResponse login(LoginRequest request) {
        TorqueDeskPrincipal principal = authenticate(request);
        String identityToken = jwtService.generateIdentityToken(principal);
        return resolveLoginResponse(principal, identityToken);
    }

    public WorkspaceSelectionResponse selectWorkspace(WorkspaceSelectionRequest request) {
        TorqueDeskPrincipal principal = securityUtils.getPrincipal();
        UserTenant workspace = loadUserTenant(request.userTenantId());
        validateUserTenant(principal, workspace);
        String workspaceToken = jwtService.generateWorkspaceToken(workspace);

        return new WorkspaceSelectionResponse(workspaceToken, workspace.getTenant().getId(), workspace.getTenant().getName(), workspace.getRole());
    }

    public RefreshResponse refresh(RefreshRequest request) {
        if (!jwtService.validateRefreshToken(request.refreshToken())) throw new AccessDeniedException("Invalid refresh token");
        UUID userId = jwtService.extractUserId(request.refreshToken());
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        validateUser(user);
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.fromUser(user);
        String identityToken = jwtService.generateIdentityToken(principal);
        return new RefreshResponse(identityToken);
    }

    private LoginResponse resolveLoginResponse(TorqueDeskPrincipal principal, String identityToken) {
        List<UserTenant> workspaces = userTenantRepository.findAllByUserId(principal.getUserPrincipal().getId());
        if (workspaces.isEmpty()) {
            if (principal.isSuperAdmin()) {
                return new LoginResponse(identityToken, null, null);
            }
            throw new UserTenantNotFoundException("No workspace available");
        }
        if (workspaces.size() == 1) {
            log.debug("Found one workspace linked from this user Id: {}", principal.getUserPrincipal().getId());
            UserTenant workspace = workspaces.getFirst();
            String workspaceToken = jwtService.generateWorkspaceToken(workspace);
            return new LoginResponse(identityToken, workspaceToken, null);
        }
        return new LoginResponse(identityToken, null, loadWorkspaces(workspaces));
    }

    private TorqueDeskPrincipal authenticate(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.login(), request.password()));
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.debug("Authenticating user: {}", ((UserPrincipal) authentication.getPrincipal()).getAuthorities());
        return TorqueDeskPrincipal.fromUser(userPrincipal);
    }

    private List<AvailableWorkspaceResponse> loadWorkspaces(List<UserTenant> workspaces) {
        return workspaces.stream().map(mapper::toResponse).toList();
    }

    private UserTenant loadUserTenant(UUID userTenantId) {
        return userTenantRepository.findById(userTenantId).orElseThrow(() -> new UserTenantNotFoundException("Workspace not found"));
    }

    private void validateUserTenant(TorqueDeskPrincipal torqueDeskPrincipal, UserTenant userTenant) {
        validateWorkspaceEnabled(userTenant);
        validateWorkspace(userTenant);
        validateOwner(torqueDeskPrincipal, userTenant);
    }

    private void validateWorkspaceEnabled(UserTenant userTenant) {
        log.debug("Validating workspace enabled status: {}", userTenant.getEnabled());
        if (!userTenant.getEnabled()) throw new UserTenantDisabledException("Workspace is disabled.");
    }

    private void validateWorkspace(UserTenant userTenant) {
        if (userTenant.getTenant().getStatus() == TenantStatus.INACTIVE) throw new TenantAlreadyDeactivatedException("Tenant is disabled");
        log.debug("Validating tenant ID: {}", userTenant.getTenant().getId());
    }

    private void validateOwner(TorqueDeskPrincipal torqueDeskPrincipal, UserTenant userTenant) {
        if (!userTenant.getUser().getId().equals(torqueDeskPrincipal.getUserPrincipal().getId()))
            throw new AccessDeniedException("Workspace does not belong to authenticated user.");
    }

    private void validateUser(User user) {
        if (!user.getEnabled()) throw new UnauthorizedException("User is disabled.");
        log.debug("Validating user ID: {}", user.getId());
    }
}