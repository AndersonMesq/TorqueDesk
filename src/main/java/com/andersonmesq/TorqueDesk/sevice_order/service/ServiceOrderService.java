package com.andersonmesq.TorqueDesk.sevice_order.service;

import com.andersonmesq.TorqueDesk.sevice_order.dto.CreateServiceOrderRequest;
import com.andersonmesq.TorqueDesk.sevice_order.dto.ServiceOrderResponse;
import com.andersonmesq.TorqueDesk.sevice_order.mapper.ServiceOrderMapper;
import com.andersonmesq.TorqueDesk.sevice_order.model.ServiceOrder;
import com.andersonmesq.TorqueDesk.sevice_order.repository.ServiceOrderRepository;
import com.andersonmesq.TorqueDesk.vehicle.exception.VehicleNotFoundException;
import com.andersonmesq.TorqueDesk.vehicle.repository.VehicleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceOrderService {
    private final VehicleRepository vehicleRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderMapper mapper;

    public ServiceOrderResponse createService(@Valid CreateServiceOrderRequest request){
        if (vehicleRepository.existsById(request.vehicleId())){
            throw new VehicleNotFoundException("Vehicle not exist");
        }
        ServiceOrder serviceOrder = ServiceOrder.builder()
                .description(request.serviceDescription())
                .build();
        serviceOrderRepository.save(serviceOrder);

        return mapper.toResponse(serviceOrder);
    }
}
