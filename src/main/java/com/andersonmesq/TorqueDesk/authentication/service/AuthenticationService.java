package com.andersonmesq.TorqueDesk.authentication.service;

import com.andersonmesq.TorqueDesk.authentication.dto.request.LoginRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.request.WorkspaceSelectionRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.response.LoginResponse;
import com.andersonmesq.TorqueDesk.authentication.dto.response.AvailableWorkspaceResponse;
import com.andersonmesq.TorqueDesk.authentication.dto.response.WorkspaceSelectionResponse;
import com.andersonmesq.TorqueDesk.security.jwt.JwtService;
import com.andersonmesq.TorqueDesk.security.principal.UserPrincipal;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.exception.AccessDeniedException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyDeactivatedException;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.usertenant.exception.UserTenantDisabledException;
import com.andersonmesq.TorqueDesk.usertenant.exception.UserTenantNotFoundException;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request) {
        UserPrincipal userPrincipal = authenticate(request);
        String identityToken = jwtService.generateIdentityToken(userPrincipal);
        List<AvailableWorkspaceResponse>workspace = loadWorkspaces(userPrincipal);
        return new LoginResponse(identityToken, workspace);
    }

    public WorkspaceSelectionResponse selectWorkspace(WorkspaceSelectionRequest request) {
        UserTenant userTenant = loadUserTenant(request.userTenantId());

        validateUserTenant(userTenant, );
        String workspaceToken = jwtService.generateWorkspaceToken(userTenant);

        return new WorkspaceSelectionResponse(workspaceToken, userTenant.getTenant().getId(), userTenant.getTenant().getName(), userTenant.getRole());
    }

    private UserPrincipal authenticate(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.login(), request.password()));
        return (UserPrincipal) authentication.getPrincipal();
    }

    private List<AvailableWorkspaceResponse> loadWorkspaces(UserPrincipal userPrincipal){
        return userTenantRepository.findAllByUserId(userPrincipal.getId()).stream().map(userTenant -> new AvailableWorkspaceResponse(
                userTenant.getId(),
                userTenant.getTenant().getId(),
                userTenant.getTenant().getName(),
                userTenant.getRole()
        )).toList();
    }

    private UserTenant loadUserTenant(UUID userTenantId){
        return userTenantRepository.findById(userTenantId).orElseThrow(() -> new UserTenantNotFoundException("Workspace not found"));
    }

    private void validateUserTenant(UserPrincipal userPrincipal, UserTenant userTenant){
        validateWorkspaceEnabled(userTenant);
        validateTenant(userTenant);
        validateOwner(userPrincipal, userTenant);
    }

    private void validateWorkspaceEnabled(UserTenant userTenant) {
        if (!userTenant.getEnabled()) throw new UserTenantDisabledException("Workspace is disabled.");
    }

    private void validateTenant(UserTenant userTenant){
        if (userTenant.getTenant().getStatus() == TenantStatus.INACTIVE) throw new TenantAlreadyDeactivatedException("Tenant is disabled");
    }

    private void validateOwner(UserPrincipal userPrincipal, UserTenant userTenant){
        if (!userTenant.getUser().getId().equals(userPrincipal.getId())) throw new AccessDeniedException("Workspace does not belong to authenticated user.");
    }
}
