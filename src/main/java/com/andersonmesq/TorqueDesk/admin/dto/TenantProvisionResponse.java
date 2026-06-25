package com.andersonmesq.TorqueDesk.admin.dto;

import java.util.UUID;

public record TenantProvisionResponse(
        UUID tenantId,
        UUID ownerId,
        String temporaryPassword
) {
}
