package com.andersonmesq.TorqueDesk.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
        @NotBlank(message = "Name of company as required")
        @Size(max = 100)
        String companyName,
        @NotBlank
        @Size(max = 100)
        String ownerName,
        @NotBlank
        @Email
        String ownerEmail
) {}
