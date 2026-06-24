package com.andersonmesq.TorqueDesk.sevice_order.mapper;

import com.andersonmesq.TorqueDesk.sevice_order.dto.ServiceOrderResponse;
import com.andersonmesq.TorqueDesk.sevice_order.model.ServiceOrder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceOrderMapper {
    ServiceOrderResponse toResponse(ServiceOrder serviceOrder);
}
