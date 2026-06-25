package com.andersonmesq.TorqueDesk.vehicle.controller;

import com.andersonmesq.TorqueDesk.vehicle.dto.UpdateVehicleRequest;
import com.andersonmesq.TorqueDesk.vehicle.dto.VehicleResponse;
import com.andersonmesq.TorqueDesk.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService service;

    @GetMapping("/{model}")
    public VehicleResponse findByModel(@PathVariable String model){
        return service.findByModel(model);
    }

    @GetMapping("/{plate}")
    public VehicleResponse findByPlate(@PathVariable String plate){
        return service.findByPlate(plate);
    }

    @PutMapping("/{id}")
    public VehicleResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateVehicleRequest request){
        return service.updateVehicle(id, request);
    }
}
