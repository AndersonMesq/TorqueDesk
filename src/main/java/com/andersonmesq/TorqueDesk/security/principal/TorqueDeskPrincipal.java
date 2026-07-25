package com.andersonmesq.TorqueDesk.security.principal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Builder
@AllArgsConstructor
public class TorqueDeskPrincipal implements UserDetails {
    private final UserPrincipal userPrincipal;
    private final WorkspacePrincipal workspacePrincipal;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        if (workspacePrincipal != null){
            return workspacePrincipal.getAuthorities();
        }
        return userPrincipal.getAuthorities();
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
}
