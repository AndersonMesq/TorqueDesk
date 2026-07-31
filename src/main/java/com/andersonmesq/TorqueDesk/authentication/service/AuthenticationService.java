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
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.exception.AccessDeniedException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyDeactivatedException;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.usertenant.exception.UserTenantDisabledException;
import com.andersonmesq.TorqueDesk.usertenant.exception.UserTenantNotFoundException;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

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
        List<UserTenant> workspaces = userTenantRepository.findAllByUserId(principal.getUserPrincipal().getId());
        if (workspaces.isEmpty()) throw new UserTenantNotFoundException("No workspace available");
        if (workspaces.size() == 1) {
            UserTenant workspace = workspaces.getFirst();
            String workspaceToken = jwtService.generateWorkspaceToken(workspace);
            return new LoginResponse(identityToken, workspaceToken, null);
        }
        return new LoginResponse(identityToken, null, loadWorkspaces(workspaces));
    }

    public WorkspaceSelectionResponse selectWorkspace(WorkspaceSelectionRequest request) {
        TorqueDeskPrincipal principal = securityUtils.getPrincipal();
        UserTenant workspace = loadUserTenant(request.userTenantId());
        validateUserTenant(principal, workspace);
        String workspaceToken = jwtService.generateWorkspaceToken(workspace);

        return new WorkspaceSelectionResponse(workspaceToken, workspace.getTenant().getId(), workspace.getTenant().getName(), workspace.getRole());
    }

    public RefreshResponse refresh(RefreshRequest request){
        if (!jwtService.validateRefreshToken(request.refreshToken())) throw new AccessDeniedException("Invalid refresh token");
        UUID userId = jwtService.extractUserId(request.refreshToken());
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        validateUser(user);
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.fromUser(user);
        String identityToken = jwtService.generateIdentityToken(principal);
        return new RefreshResponse(identityToken);
    }

    private TorqueDeskPrincipal authenticate(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.login(), request.password()));
        return (TorqueDeskPrincipal) authentication.getPrincipal();
    }

    private List<AvailableWorkspaceResponse> loadWorkspaces(List<UserTenant> workspaces){
        return workspaces.stream().map(mapper::toResponse).toList();
    }

    private UserTenant loadUserTenant(UUID userTenantId){
        return userTenantRepository.findById(userTenantId).orElseThrow(() -> new UserTenantNotFoundException("Workspace not found"));
    }

    private void validateUserTenant(TorqueDeskPrincipal torqueDeskPrincipal, UserTenant userTenant){
        validateWorkspaceEnabled(userTenant);
        validateTenant(userTenant);
        validateOwner(torqueDeskPrincipal, userTenant);
    }

    private void validateWorkspaceEnabled(UserTenant userTenant) {
        if (!userTenant.getEnabled()) throw new UserTenantDisabledException("Workspace is disabled.");
    }

    private void validateTenant(UserTenant userTenant){
        if (userTenant.getTenant().getStatus() == TenantStatus.INACTIVE) throw new TenantAlreadyDeactivatedException("Tenant is disabled");
    }

    private void validateOwner(TorqueDeskPrincipal torqueDeskPrincipal, UserTenant userTenant){
        if (!userTenant.getUser().getId().equals(torqueDeskPrincipal.getUserPrincipal().getId())) throw new AccessDeniedException("Workspace does not belong to authenticated user.");
    }

    private void validateUser(User user){
        if (!user.getEnabled()) throw new UnauthorizedException("User is disabled.");
    }
}