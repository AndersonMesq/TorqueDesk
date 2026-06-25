package com.andersonmesq.TorqueDesk.admin.mapper;

import com.andersonmesq.TorqueDesk.user.dto.UserResponse;
import com.andersonmesq.TorqueDesk.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {
    UserResponse toResponse(User user);
}