package com.andersonmesq.TorqueDesk.vehicle.service;

import com.andersonmesq.TorqueDesk.vehicle.dto.UpdateVehicleRequest;
import com.andersonmesq.TorqueDesk.vehicle.dto.VehicleResponse;
import com.andersonmesq.TorqueDesk.vehicle.exception.VehicleNotFoundException;
import com.andersonmesq.TorqueDesk.vehicle.mapper.VehicleMapper;
import com.andersonmesq.TorqueDesk.vehicle.model.Vehicle;
import com.andersonmesq.TorqueDesk.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional (readOnly = true)
public class VehicleService {
    private final VehicleRepository repository;
    private final VehicleMapper mapper;

    public VehicleResponse findById(UUID id) {
        Vehicle vehicle = repository.findById(id).orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));
        return mapper.toResponse(vehicle);
    }

    public VehicleResponse findByPlate(String plate){
        Vehicle vehicles = repository.findByPlate(plate).orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));
        return mapper.toResponse(vehicles);
    }

    public VehicleResponse findByModel(String model){
        Vehicle vehicles = repository.findByModel(model).orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));
        return mapper.toResponse(vehicles);
    }

    @Transactional
    public VehicleResponse updateVehicle(UUID id, UpdateVehicleRequest request){
        Vehicle vehicles = repository.findById(id).orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));
        vehicles.setPlate(request.plate());
        return mapper.toResponse(vehicles);
    }
}