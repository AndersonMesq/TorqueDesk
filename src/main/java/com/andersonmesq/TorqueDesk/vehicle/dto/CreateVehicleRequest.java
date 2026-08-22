package com.andersonmesq.TorqueDesk.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateVehicleRequest(
        @NotNull
        UUID customerId,

        @NotBlank
        String name,

        @NotBlank
        String brand,

        @NotBlank
        String model,

        @NotNull
        Integer modelYear,

        @NotBlank
        String plate
) {
}