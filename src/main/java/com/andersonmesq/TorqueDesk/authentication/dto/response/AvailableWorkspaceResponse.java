package com.andersonmesq.TorqueDesk.authentication.dto.response;

import com.andersonmesq.TorqueDesk.usertenant.role.Role;

import java.util.UUID;

public record AvailableWorkspaceResponse(
        UUID userTenantId,
        UUID tenantId,
        String tenantName,
        Role role
) {
}
