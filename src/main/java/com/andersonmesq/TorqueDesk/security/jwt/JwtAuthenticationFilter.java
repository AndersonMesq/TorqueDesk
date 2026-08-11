package com.andersonmesq.TorqueDesk.security.jwt;

import com.andersonmesq.TorqueDesk.security.exception.InvalidTokenException;
import com.andersonmesq.TorqueDesk.security.principal.TorqueDeskPrincipal;
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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserTenantRepository userTenantRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,@NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        log.debug("Header extract from request.getHeader(\"Authorization\"): {}", header);
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
        TorqueDeskPrincipal principal;
        switch (tokenType) {
            case IDENTITY -> {
                if (!jwtService.validateIdentityToken(token)) {
                    throw new InvalidTokenException("Invalid identity token");
                }
                principal = buildIdentityPrincipal(token);
                log.debug("TorqueDeskPrincipal getAuthorities in authenticate case IDENTITY:{}", principal.getAuthorities());
            }
            case WORKSPACE -> {
                if (!jwtService.validateWorkspaceToken(token)) {
                    throw new InvalidTokenException("Invalid workspace token");
                }
                principal = buildWorkspacePrincipal(token);
                log.debug("TorqueDeskPrincipal getAuthorities in authenticate case WORKSPACE:{}", principal.getAuthorities());
            }
            default -> throw new InvalidTokenException("Unsupported token type");
        }
        setAuthentication(principal);
    }

    private TorqueDeskPrincipal buildIdentityPrincipal(String token) {
        UUID userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        log.info("IdentityPrincipal built");
        return  TorqueDeskPrincipal.builder()
                .userPrincipal(userPrincipal)
                .workspacePrincipal(null)
                .build();
    }

    private TorqueDeskPrincipal buildWorkspacePrincipal(String token) {
        UUID userTenantId = jwtService.extractUserTenantId(token);
        UserTenant userTenant = userTenantRepository.findById(userTenantId).orElseThrow(() -> new UserTenantNotFoundException("UserTenant not found"));
        UserPrincipal userPrincipal = UserPrincipal.create(userTenant.getUser());
        WorkspacePrincipal workspacePrincipal = WorkspacePrincipal.create(userTenant);
        log.info("WorkspacePrincipal built");
        return TorqueDeskPrincipal.builder()
                .userPrincipal(userPrincipal)
                .workspacePrincipal(workspacePrincipal)
                .build();
    }

    private void setAuthentication(TorqueDeskPrincipal principal) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        log.debug("authentication set");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}