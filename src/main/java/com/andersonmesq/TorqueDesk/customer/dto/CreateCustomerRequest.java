package com.andersonmesq.TorqueDesk.customer.dto;

import java.util.UUID;

public record CreateCustomerRequest(
        String name,
        String email,
        String phone,
        UUID tenantId
) {
}
