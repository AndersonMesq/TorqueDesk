package com.andersonmesq.TorqueDesk.security.principal;

import com.andersonmesq.TorqueDesk.usertenant.role.Role;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class WorkspacePrincipal implements UserDetails {
    private final UUID userId;
    private final UUID tenantId;
    private final UUID userTenantId;
    private final Role role;
    private final String username;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public static WorkspacePrincipal create(UserTenant userTenant){
        return new WorkspacePrincipal(
                userTenant.getUser().getId(),
                userTenant.getTenant().getId(),
                userTenant.getId(),
                userTenant.getRole(),
                userTenant.getUser().getUserName(),
                userTenant.getEnabled(),
                List.of(new SimpleGrantedAuthority("ROLE_" + userTenant.getRole().name()))
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return authorities;
    }

    @Override
    public String getPassword(){
        return null;
    }

    @Override
    public String getUsername(){
        return username;
    }

    public boolean isOwner(){
        return role == Role.OWNER;
    }

    public boolean isMechanic(){
        return role == Role.MECHANIC;
    }

    public boolean isAttendant(){
        return role == Role.ATTENDANT;
    }

    public boolean hasRole(Role role){
        return this.role == role;
    }
}
