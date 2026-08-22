package com.andersonmesq.TorqueDesk.security.principal;

import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@Getter
@Builder
@AllArgsConstructor
public class TorqueDeskPrincipal implements UserDetails {
    private final UserPrincipal userPrincipal;
    private final WorkspacePrincipal workspacePrincipal;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        if (workspacePrincipal != null){
            log.debug("workspacePrincipal is null");
            return workspacePrincipal.getAuthorities();
        }
        return userPrincipal.getAuthorities();
    }

    public static TorqueDeskPrincipal fromUser(UserPrincipal userPrincipal){
        return TorqueDeskPrincipal.builder()
                .userPrincipal(UserPrincipal.create(userPrincipal))
                .workspacePrincipal(null)
                .build();
    }

    public static TorqueDeskPrincipal fromUser(User user){
        return TorqueDeskPrincipal.builder()
                .userPrincipal(UserPrincipal.create(user))
                .workspacePrincipal(null)
                .build();
    }

    @Override
    public String getPassword() {
        return userPrincipal.getPassword();
    }

    @Override
    public String getUsername() {
        return userPrincipal.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return userPrincipal.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return userPrincipal.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return userPrincipal.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return userPrincipal.isEnabled();
    }

    public boolean hasWorkspace() {
        return workspacePrincipal != null;
    }

    public boolean isSuperAdmin() {
        return userPrincipal.getSystemRole() == SystemRole.SUPER_ADMIN;
    }

    public UUID getUserId() {
        return userPrincipal.getId();
    }

    public UUID getTenantId() {
        if (workspacePrincipal == null) {
            throw new IllegalStateException("Workspace not selected.");
        }
        return workspacePrincipal.getTenantId();
    }
}