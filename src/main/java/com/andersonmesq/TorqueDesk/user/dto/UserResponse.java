package com.andersonmesq.TorqueDesk.user.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String fullName,
        String email,
        Boolean enabled
) {
}