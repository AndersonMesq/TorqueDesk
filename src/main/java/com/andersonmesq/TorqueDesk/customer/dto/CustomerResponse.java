package com.andersonmesq.TorqueDesk.customer.dto;

import java.util.UUID;

public record CustomerResponse (
        UUID id,
        String name,
        String email
){
}
