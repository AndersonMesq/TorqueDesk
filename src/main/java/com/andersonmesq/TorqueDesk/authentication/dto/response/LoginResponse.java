package com.andersonmesq.TorqueDesk.authentication.dto.response;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String identityToken,
        List<AvailableWorkspaceResponse> tenants
) {
}
