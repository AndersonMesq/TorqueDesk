package com.andersonmesq.TorqueDesk.security.jwt;

import com.andersonmesq.TorqueDesk.security.exception.InvalidTokenException;
import com.andersonmesq.TorqueDesk.security.principal.UserPrincipal;
import com.andersonmesq.TorqueDesk.security.principal.WorkspacePrincipal;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.usertenant.exception.UserTenantNotFoundException;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserTenantRepository userTenantRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);

        authenticate(token);

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token) {
        JwtTokenType tokenType = jwtService.extractTokenType(token);

        switch (tokenType) {
            case IDENTITY -> {
                if (!jwtService.validateIdentityToken(token)) {
                    throw new InvalidTokenException("Invalid identity token");
                }
                authenticateIdentity(token);
            }
            case WORKSPACE -> {
                if (!jwtService.validateWorkspaceToken(token)) {
                    throw new InvalidTokenException("Invalid workspace token");
                }
                authenticateWorkspace(token);
            }
        }
    }

    private void authenticateIdentity(String token) {
        UUID userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserPrincipal principal = UserPrincipal.create(user);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private void authenticateWorkspace(String token) {
        UUID userTenantId = jwtService.extractUserTenantId(token);
        UserTenant userTenant = userTenantRepository.findById(userTenantId).orElseThrow(() -> new UserTenantNotFoundException("UserTenant not found"));
        WorkspacePrincipal principal = WorkspacePrincipal.create(userTenant);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}