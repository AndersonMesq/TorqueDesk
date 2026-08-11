package com.andersonmesq.TorqueDesk.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "User name is required")
        @Size(max = 100)
        String fullName,

        @NotBlank(message = "User name for login is required")
        @Size(max = 100)
        String userName,

        @NotBlank(message = "Email is required")
        @Email
        String email,
        @NotBlank(message = "Password is required")
        String password
) {
}