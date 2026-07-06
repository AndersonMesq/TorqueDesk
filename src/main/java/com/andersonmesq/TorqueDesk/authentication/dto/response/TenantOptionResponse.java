package com.andersonmesq.TorqueDesk.authentication.dto.response;

import java.util.UUID;

public record TenantOptionResponse(
        UUID id,
        String companyName
) {
}
