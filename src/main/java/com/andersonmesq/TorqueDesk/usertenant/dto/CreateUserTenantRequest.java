package com.andersonmesq.TorqueDesk.usertenant.dto;


import com.andersonmesq.TorqueDesk.usertenant.enums.Role;

import java.util.UUID;

public record CreateUserTenantRequest(
        UUID userId,
        UUID tenantId,
        Role role
) {
}