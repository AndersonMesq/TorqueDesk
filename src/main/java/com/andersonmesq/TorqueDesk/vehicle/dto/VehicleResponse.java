package com.andersonmesq.TorqueDesk.vehicle.dto;

import com.andersonmesq.TorqueDesk.vehicle.model.Vehicle;

import java.util.UUID;

public record VehicleResponse(
        UUID id,
        String Brand,
        String Model,
        Integer Year,
        String Plate
) {
}
