package com.andersonmesq.TorqueDesk.admin.mapper;

import com.andersonmesq.TorqueDesk.tenant.dto.TenantResponse;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminTenantMapper {
    TenantResponse toResponse(Tenant tenant);
}