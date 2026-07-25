package com.andersonmesq.TorqueDesk.authentication.dto.response;

import com.andersonmesq.TorqueDesk.usertenant.enums.Role;

import java.util.UUID;

public record WorkspaceSelectionResponse(
        String workspaceToken,
        UUID tenantId,
        String tenantName,
        Role role
) {
}
