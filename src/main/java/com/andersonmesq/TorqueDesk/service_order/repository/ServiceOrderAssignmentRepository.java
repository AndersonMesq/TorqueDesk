package com.andersonmesq.TorqueDesk.service_order.repository;

import com.andersonmesq.TorqueDesk.service_order.model.ServiceOrderAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderAssignmentRepository extends JpaRepository<ServiceOrderAssignment, UUID> {
    boolean existsByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(UUID serviceOrderId, UUID employeeId);
    Optional<ServiceOrderAssignment>
    findByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(UUID serviceOrderId, UUID employeeId);
}