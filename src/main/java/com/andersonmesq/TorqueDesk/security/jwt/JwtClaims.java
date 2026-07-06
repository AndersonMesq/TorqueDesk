package com.andersonmesq.TorqueDesk.security.jwt;

import com.andersonmesq.TorqueDesk.usertenant.enums.Role;

import java.util.UUID;

public record JwtClaims(
        UUID userId,
        UUID TenantId,
        Role role,
        JwtTokenType tokenType
) {
}
