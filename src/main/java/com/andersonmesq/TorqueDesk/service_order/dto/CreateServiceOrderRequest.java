package com.andersonmesq.TorqueDesk.service_order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateServiceOrderRequest(
        @NotBlank
        @Size(max = 300)
        String serviceDescription,

        @NotNull
        UUID vehicleId
) {
}