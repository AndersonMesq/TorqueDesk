package com.andersonmesq.TorqueDesk.security.jwt;

import com.andersonmesq.TorqueDesk.security.principal.UserPrincipal;
import com.andersonmesq.TorqueDesk.usertenant.enums.Role;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;

//Atualmente secretKey é recriado apos toda requisição

@Service
@RequiredArgsConstructor
public class JwtService {
    private final SecretKey secretKey;
    private final JwtProperties properties;

    public String generateIdentityToken(UserPrincipal userPrincipal) {
        Instant now = Instant.now();

        return Jwts.builder()
                .claim(JwtClaims.TOKEN_TYPE, JwtTokenType.IDENTITY.name())
                .claim(JwtClaims.USER_ID, userPrincipal.getId())
                .subject(userPrincipal.getLogin())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(getIdentityExpiration(), SECONDS)))
                .signWith(getKey())
                .compact();
    }

    public String generateWorkspaceToken(UserTenant userTenant) {
        Instant now = Instant.now();

        return Jwts.builder()
                .claim(JwtClaims.TOKEN_TYPE, JwtTokenType.WORKSPACE.name())
                .claim(JwtClaims.USER_ID, userTenant.getUser().getId())
                .claim(JwtClaims.TENANT_ID, userTenant.getTenant().getId())
                .claim(JwtClaims.USER_TENANT_ID, userTenant.getId())
                .claim(JwtClaims.ROLE, userTenant.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(getWorkspaceExpiration(), SECONDS)))
                .signWith(getKey())
                .compact();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parse().parseSignedClaims(token).getPayload().getSubject());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public JwtTokenType extractTokenType(String token){
        return JwtTokenType.valueOf(extractClaims(token).get(JwtClaims.TOKEN_TYPE, String.class));
    }

    public UUID extractUserTenantId(String token){
        Claims claims = extractClaims(token);
        return UUID.fromString(claims.get(JwtClaims.USER_TENANT_ID, String.class));
    }

    public Role extractRole(String token){
        String role = extractClaims(token).toString();
        return Role.valueOf(role);
    }

    public boolean validateIdentityToken(String token) {
        return validateToken(token, JwtTokenType.IDENTITY);
    }

    public boolean validateWorkspaceToken(String token) {
        return validateToken(token, JwtTokenType.WORKSPACE);
    }

    private boolean validateToken(String token, JwtTokenType expectedType) {
        Claims claims = extractClaims(token);
        String tokenType = claims.get(JwtClaims.TOKEN_TYPE, String.class);
        return expectedType.name().equals(tokenType);
    }

    private JwtParser parse() {
        return Jwts.parser().verifyWith(getKey()).build();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(properties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }

    private Long getIdentityExpiration() {
        return properties.getIdentityExpiration();
    }

    private Long getWorkspaceExpiration() {
        return properties.getWorkspaceExpiration();
    }
}
