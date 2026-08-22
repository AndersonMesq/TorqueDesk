package com.andersonmesq.TorqueDesk.vehicle.service;

import com.andersonmesq.TorqueDesk.customer.exception.CustomerNotFoundException;
import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import com.andersonmesq.TorqueDesk.vehicle.dto.CreateVehicleRequest;
import com.andersonmesq.TorqueDesk.vehicle.dto.UpdateVehicleRequest;
import com.andersonmesq.TorqueDesk.vehicle.dto.VehicleResponse;
import com.andersonmesq.TorqueDesk.vehicle.exception.VehicleAlreadyExistException;
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
    private final CustomerRepository customerRepository;
    private final VehicleRepository repository;
    private final VehicleMapper mapper;

    private Vehicle findVehicle(UUID id) {
        return repository.findById(id).orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));
    }

    @Transactional
    public VehicleResponse createVehicle(CreateVehicleRequest request) throws VehicleNotFoundException {
        if (repository.existsByPlateAndModel(request.plate(), request.model())) throw new VehicleAlreadyExistException("Vehicle already exists");
        Customer customer = customerRepository.findById(request.customerId()).orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        Vehicle vehicle = Vehicle.builder()
                .brand(request.brand())
                .model(request.model())
                .modelYear(request.modelYear())
                .plate(request.plate())
                .customer(customer)
                .build();
        repository.save(vehicle);
        return mapper.toResponse(vehicle);
    }

    public VehicleResponse findById(UUID id) {
        Vehicle vehicle = findVehicle(id);
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
        Vehicle vehicles = findVehicle(id);
        vehicles.setPlate(request.plate());
        return mapper.toResponse(vehicles);
    }
}