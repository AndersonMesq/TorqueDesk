package com.andersonmesq.TorqueDesk.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank
        String fullName
) {
}