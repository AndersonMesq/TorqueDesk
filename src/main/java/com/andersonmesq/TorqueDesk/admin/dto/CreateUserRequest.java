package com.andersonmesq.TorqueDesk.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "User name is required")
        @Size(max = 100)
        String fullName,

        @Size(max = 100)
        String userName,

        @Email
        String email,

        @NotBlank(message = "Password is required")
        String password
) {
}