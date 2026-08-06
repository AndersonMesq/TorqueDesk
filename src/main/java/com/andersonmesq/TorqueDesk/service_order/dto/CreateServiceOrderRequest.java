package com.andersonmesq.TorqueDesk.service_order.dto;

import java.util.UUID;

public record CreateServiceOrderRequest(
        UUID vehicleId,
        String serviceDescription
) {
}
