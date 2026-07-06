package com.andersonmesq.TorqueDesk.authentication.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SelectTenantRequest(
        @NotNull
        UUID userId,

        @NotNull
        UUID tenantId
) {
}
