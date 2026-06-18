package com.andersonmesq.TorqueDesk.usertenant.mapper;

import com.andersonmesq.TorqueDesk.usertenant.dto.UserTenantResponse;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserTenantMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "tenant.id", target = "tenantId")
    UserTenantResponse toResponse(UserTenant userTenant);
}