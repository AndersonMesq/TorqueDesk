package com.andersonmesq.TorqueDesk.authentication.mapper;

import com.andersonmesq.TorqueDesk.authentication.dto.response.AvailableWorkspaceResponse;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AvailableWorkspaceMapper {
    AvailableWorkspaceResponse toResponse(UserTenant userTenant);
}
