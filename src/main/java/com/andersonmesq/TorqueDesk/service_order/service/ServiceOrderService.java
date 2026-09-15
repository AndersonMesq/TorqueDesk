package com.andersonmesq.TorqueDesk.service_order.service;

import com.andersonmesq.TorqueDesk.security.context.SecurityUtils;
import com.andersonmesq.TorqueDesk.security.principal.TorqueDeskPrincipal;
import com.andersonmesq.TorqueDesk.service_order.dto.CreateServiceOrderRequest;
import com.andersonmesq.TorqueDesk.service_order.dto.ServiceOrderResponse;
import com.andersonmesq.TorqueDesk.service_order.exception.ServiceOrderAssignmentAlreadyFinishedException;
import com.andersonmesq.TorqueDesk.service_order.exception.ServiceOrderAssignmentAlreadyStartedException;
import com.andersonmesq.TorqueDesk.service_order.exception.ServiceOrderNotFoundException;
import com.andersonmesq.TorqueDesk.service_order.mapper.ServiceOrderMapper;
import com.andersonmesq.TorqueDesk.service_order.model.ServiceOrder;
import com.andersonmesq.TorqueDesk.service_order.model.ServiceOrderAssignment;
import com.andersonmesq.TorqueDesk.service_order.repository.ServiceOrderAssignmentRepository;
import com.andersonmesq.TorqueDesk.service_order.repository.ServiceOrderRepository;
import com.andersonmesq.TorqueDesk.service_order.service_order_status.ServiceOrderStatus;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.vehicle.exception.VehicleNotFoundException;
import com.andersonmesq.TorqueDesk.vehicle.model.Vehicle;
import com.andersonmesq.TorqueDesk.vehicle.repository.VehicleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceOrderService {
    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderAssignmentRepository assignmentRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final ServiceOrderMapper mapper;

    private Vehicle findVehicle(UUID vehicleId) {
        return vehicleRepository.findById(vehicleId).orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private ServiceOrder findServiceOrder(UUID serviceOrderId) {
        return serviceOrderRepository.findById(serviceOrderId).orElseThrow(() -> new ServiceOrderNotFoundException("Service order not found"));
    }

    @Transactional
    public ServiceOrderResponse create(@Valid CreateServiceOrderRequest request) {
        TorqueDeskPrincipal principal = securityUtils.getPrincipal();
        Vehicle vehicle = findVehicle(request.vehicleId());
        User createBy = findUser(principal.getUserId());
        ServiceOrder serviceOrder = ServiceOrder.builder()
                .description(request.serviceDescription())
                .vehicle(vehicle)
                .createdBy(createBy)
                .status(ServiceOrderStatus.OPEN)
                .build();
        log.debug("Service order created. serviceOrderId={}, vehicleId={}, createdBy={}", serviceOrder.getId(), vehicle.getId(), createBy.getId());
        serviceOrderRepository.save(serviceOrder);
        return mapper.toResponse(serviceOrder);
    }

    @Transactional
    public ServiceOrderResponse startAssignment(UUID serviceOrderId) {
        TorqueDeskPrincipal principal = securityUtils.getPrincipal();
        ServiceOrder serviceOrder = findServiceOrder(serviceOrderId);
        UUID employeeId = principal.getUserId();
        if (assignmentRepository.existsByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(serviceOrderId, employeeId))
            throw new ServiceOrderAssignmentAlreadyStartedException("Assignment already started");
        User employee = findUser(employeeId);
        ServiceOrderAssignment assignment = ServiceOrderAssignment.builder()
                .serviceOrder(serviceOrder)
                .employee(employee)
                .startedAt(LocalDateTime.now())
                .build();
        assignmentRepository.save(assignment);
        return mapper.toResponse(serviceOrder);
    }

    @Transactional
    public void finishAssignment(UUID serviceOrderId) {
        TorqueDeskPrincipal principal = securityUtils.getPrincipal();
        ServiceOrderAssignment assignment = assignmentRepository.findByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(serviceOrderId, principal.getUserId())
                .orElseThrow(() -> new ServiceOrderNotFoundException("ServiceOrder not found"));
        assignment.setFinishedAt(LocalDateTime.now());
        log.debug("Employee {} finished assignment {}", principal.getUserId(), assignment.getId());
    }
}