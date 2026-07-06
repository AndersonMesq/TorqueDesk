package com.andersonmesq.TorqueDesk.authentication.dto.response;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        UUID userId,
        String fullName,
        List<TenantOptionResponse> tenants
) {
}
