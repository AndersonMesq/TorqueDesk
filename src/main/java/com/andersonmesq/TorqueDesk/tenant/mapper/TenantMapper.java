package com.andersonmesq.TorqueDesk.tenant.mapper;

import com.andersonmesq.TorqueDesk.tenant.dto.TenantResponse;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TenantMapper {
    TenantResponse toResponse(
            Tenant tenant
    );
}