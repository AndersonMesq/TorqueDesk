package com.andersonmesq.TorqueDesk.security.context;

import com.andersonmesq.TorqueDesk.security.exception.UnauthorizedException;
import com.andersonmesq.TorqueDesk.security.principal.TorqueDeskPrincipal;
import com.andersonmesq.TorqueDesk.security.principal.UserPrincipal;
import com.andersonmesq.TorqueDesk.security.principal.WorkspacePrincipal;
import com.andersonmesq.TorqueDesk.usertenant.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityUtils {

    public TorqueDeskPrincipal getPrincipal(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null){
            throw new UnauthorizedException("User not authenticated.");
        }
        return (TorqueDeskPrincipal) authentication.getPrincipal();
    }

    public UserPrincipal getCurrentUser(){
        return getPrincipal().getUserPrincipal();
    }

    public WorkspacePrincipal getCurrentWorkspace(){
        TorqueDeskPrincipal principal = getPrincipal();
        if (!principal.hasWorkspace()) throw new UnauthorizedException("Workspace not selected");
        return principal.getWorkspacePrincipal();
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public UUID getCurrentTenantId() {
        return getCurrentWorkspace().getTenantId();
    }

    public UUID getCurrentUserTenantId() {
        return getCurrentWorkspace().getUserTenantId();
    }

    public Role getCurrentRole() {
        return getCurrentWorkspace().getRole();
    }

    public boolean hasWorkspace() {
        return getPrincipal().hasWorkspace();
    }

    public boolean hasRole(Role role) {
        return getCurrentWorkspace().getRole().equals(role);
    }
}