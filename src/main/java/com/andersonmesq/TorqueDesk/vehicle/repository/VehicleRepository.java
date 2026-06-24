package com.andersonmesq.TorqueDesk.vehicle.repository;

import com.andersonmesq.TorqueDesk.vehicle.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    boolean existsById(UUID id);
    Optional<Vehicle> findByPlate(String plate);
    Optional<Vehicle> findByModel(String model);
}
