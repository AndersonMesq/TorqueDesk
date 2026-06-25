package com.andersonmesq.TorqueDesk.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateTenantRequest(
        @NotBlank
        String companyName,
        @NotBlank
        String ownerName,
        @NotBlank
        @Email
        String ownerEmail
) {}
