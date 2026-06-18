package com.andersonmesq.TorqueDesk.tenant.dto;


import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;

import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        String slug,
        TenantStatus status
) {
}
