package com.andersonmesq.TorqueDesk.vehicle.mapper;

import com.andersonmesq.TorqueDesk.vehicle.dto.VehicleResponse;
import com.andersonmesq.TorqueDesk.vehicle.model.Vehicle;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
    VehicleResponse toResponse(Vehicle vehicles);
}
