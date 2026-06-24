package com.andersonmesq.TorqueDesk.sevice_order.dto;

import java.util.UUID;

public record CreateServiceOrderRequest(
        UUID vehicleId,
        String serviceDescription
) {
}
