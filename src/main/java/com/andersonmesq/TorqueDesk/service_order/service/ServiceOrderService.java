package com.andersonmesq.TorqueDesk.service_order.service;

import com.andersonmesq.TorqueDesk.service_order.dto.CreateServiceOrderRequest;
import com.andersonmesq.TorqueDesk.service_order.dto.ServiceOrderResponse;
import com.andersonmesq.TorqueDesk.service_order.mapper.ServiceOrderMapper;
import com.andersonmesq.TorqueDesk.service_order.model.ServiceOrder;
import com.andersonmesq.TorqueDesk.service_order.repository.ServiceOrderRepository;
import com.andersonmesq.TorqueDesk.vehicle.exception.VehicleNotFoundException;
import com.andersonmesq.TorqueDesk.vehicle.repository.VehicleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ServiceOrderService {
    private final VehicleRepository vehicleRepository;
    private final ServiceOrderMapper mapper;

    public ServiceOrderResponse createService(@Valid CreateServiceOrderRequest request){
        if (!vehicleRepository.existsById(request.vehicleId())){
            log.debug("Vehicle id {} does not exist", request.vehicleId());
            throw new VehicleNotFoundException("Vehicle not exist");
        }
        ServiceOrder serviceOrder = ServiceOrder.builder()
                .description(request.serviceDescription())
                .build();

        return mapper.toResponse(serviceOrder);
    }
}
