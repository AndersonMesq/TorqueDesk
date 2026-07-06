package com.andersonmesq.TorqueDesk.security.jwt;

import com.andersonmesq.TorqueDesk.security.user.UserPrincipal;
import com.andersonmesq.TorqueDesk.usertenant.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties properties;

    public String generateAuthToken(UserPrincipal userPrincipal){
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userPrincipal.getId().toString())
                .claim("type", JwtTokenType.AUTH.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getExpiration(), SECONDS)))
                .signWith(getKey())
                .compact();
    }

    public String generateAccessToken(UserPrincipal userPrincipal, UUID tenantId, Role role){
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userPrincipal.getId().toString())
                .claim("tenantId", tenantId)
                .claim("role", role.name())
                .claim("type", JwtTokenType.ACCESS.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getExpiration(), SECONDS)))
                .signWith(getKey())
                .compact();
    }

    public UUID extractUserId(String token){
        return UUID.fromString(parse().parseSignedClaims(token).getPayload().getSubject());
    }

    public JwtClaims extractClaims(String token){
        Claims claims = parse().parseSignedClaims(token).getPayload();

        UUID tenantId = claims.get("tenantId") == null ? null : UUID.fromString(claims.get("tenantId").toString());
        Role role = claims.get("role") == null ? null : Role.valueOf(claims.get("role").toString());
        JwtTokenType tokenType = JwtTokenType.valueOf(claims.get("type", String.class));

        return new JwtClaims(UUID.fromString(claims.getSubject()), tenantId, role, tokenType);
    }

    public boolean isValid(String token){
        try {
            parse().parseSignedClaims(token);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    private JwtParser parse(){
        return Jwts.parser().verifyWith(getKey()).build();
    }

    private SecretKey getKey(){
        return Keys.hmacShaKeyFor(properties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }
}
