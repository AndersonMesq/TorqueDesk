package com.andersonmesq.TorqueDesk.authentication.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        String login,

        @NotBlank
        String password
) {
}
