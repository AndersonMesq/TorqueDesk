package com.andersonmesq.TorqueDesk.security.config;

import com.andersonmesq.TorqueDesk.security.jwt.JwtProperties;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {
    private final JwtProperties properties;

    @Bean
    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(properties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }
}
