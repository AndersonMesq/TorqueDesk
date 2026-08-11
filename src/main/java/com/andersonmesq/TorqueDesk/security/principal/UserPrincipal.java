package com.andersonmesq.TorqueDesk.security.principal;

import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
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
public class UserPrincipal implements UserDetails {
    private final UUID id;
    private final String login;
    private final String password;
    private final SystemRole systemRole;

    public static UserPrincipal create(UserPrincipal userPrincipal){
        return new UserPrincipal(
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getPassword(),
                userPrincipal.getSystemRole()
        );
    }

    public static UserPrincipal create(User user){
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getSystemRole()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of(new SimpleGrantedAuthority("ROLE_" + systemRole.name()));
    }

    @Override
    public String getUsername(){
        return login;
    }

    @Override
    public String getPassword(){
        return password;
    }
}