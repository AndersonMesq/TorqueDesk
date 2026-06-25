package com.andersonmesq.TorqueDesk.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateTenantRequest(
        @NotBlank
        String companyName,
        @NotBlank
        String ownerName,
        @NotBlank
        @Email
        String ownerEmail,
        @NotBlank
        String ownerPassword
) {}
