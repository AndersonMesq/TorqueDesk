package com.andersonmesq.TorqueDesk.service_order.mapper;

import com.andersonmesq.TorqueDesk.service_order.dto.ServiceOrderResponse;
import com.andersonmesq.TorqueDesk.service_order.model.ServiceOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceOrderMapper {
    @Mapping(source = "id", target = "serviceId")
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "vehicle.customer.name", target = "customerName")
    @Mapping(source = "vehicle.model", target = "vehicleName")
    @Mapping(source = "description", target = "serviceName")
    ServiceOrderResponse toResponse(ServiceOrder serviceOrder);
}
