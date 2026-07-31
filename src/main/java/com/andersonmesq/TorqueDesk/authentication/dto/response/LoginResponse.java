package com.andersonmesq.TorqueDesk.authentication.dto.response;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String identityToken,
        String workspaceToken,
        List<AvailableWorkspaceResponse> workspaces
) {
}
