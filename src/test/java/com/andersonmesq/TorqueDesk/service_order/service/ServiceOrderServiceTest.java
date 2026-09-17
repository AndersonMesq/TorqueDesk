package com.andersonmesq.TorqueDesk.service_order.service;

import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.security.context.SecurityUtils;
import com.andersonmesq.TorqueDesk.security.exception.UnauthorizedException;
import com.andersonmesq.TorqueDesk.security.principal.TorqueDeskPrincipal;
import com.andersonmesq.TorqueDesk.service_order.dto.CreateServiceOrderRequest;
import com.andersonmesq.TorqueDesk.service_order.dto.ServiceOrderResponse;
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
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceOrderServiceTest {
    @Mock
    ServiceOrderRepository serviceOrderRepository;

    @Mock
    ServiceOrderAssignmentRepository assignmentRepository;

    @Mock
    VehicleRepository vehicleRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    SecurityUtils securityUtils;

    @Mock
    ServiceOrderMapper serviceOrderMapper;

    @InjectMocks
    ServiceOrderService serviceOrderService;

    @Test
    void shouldCreateServiceOrderWhenRequestIsValid(){
        UUID userPrincipalId = UUID.randomUUID();
        User user = User.builder()
                .id(userPrincipalId)
                .build();
        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .name("John Test")
                .build();
        Vehicle vehicle = Vehicle.builder()
                .id(UUID.randomUUID())
                .model("Test Model")
                .build();
        CreateServiceOrderRequest request = new CreateServiceOrderRequest(
                "Service Description Test",
                vehicle.getId()
        );
        ServiceOrder serviceOrder = ServiceOrder.builder()
                .id(UUID.randomUUID())
                .build();
        ServiceOrderResponse expectedResponse = new ServiceOrderResponse(
                serviceOrder.getId(),
                vehicle.getId(),
                customer.getName(),
                vehicle.getModel(),
                request.serviceDescription()
        );
        TorqueDeskPrincipal principal = mock(TorqueDeskPrincipal.class);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(vehicleRepository.findById(request.vehicleId())).thenReturn(Optional.of(vehicle));
        when(userRepository.findById(principal.getUserId())).thenReturn(Optional.of(user));
        when(serviceOrderMapper.toResponse(any(ServiceOrder.class))).thenReturn(expectedResponse);

        ServiceOrderResponse serviceOrderResponse = serviceOrderService.create(request);

        ArgumentCaptor<ServiceOrder> captor = ArgumentCaptor.forClass(ServiceOrder.class);
        verify(serviceOrderRepository).save(captor.capture());
        ServiceOrder savedServiceOrder = captor.getValue();
        assertThat(savedServiceOrder.getDescription()).isEqualTo(request.serviceDescription());
        assertThat(savedServiceOrder.getVehicle()).isEqualTo(vehicle);
        assertThat(savedServiceOrder.getCreatedBy()).isEqualTo(user);
        assertThat(savedServiceOrder.getStatus()).isEqualTo(ServiceOrderStatus.OPEN);
        assertThat(serviceOrderResponse).isEqualTo(expectedResponse);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserNotAuthenticated() {
        CreateServiceOrderRequest request = new CreateServiceOrderRequest(
                "Service Description Test",
                UUID.randomUUID()
        );
        when(securityUtils.getPrincipal()).thenThrow(new UnauthorizedException("User not authenticated"));

        ThrowableAssert.ThrowingCallable action = () -> serviceOrderService.create(request);

        assertThatThrownBy(action).isInstanceOf(UnauthorizedException.class).hasMessage("User not authenticated");
        verify(serviceOrderRepository, never()).save(any(ServiceOrder.class));
    }

    @Test
    void shouldThrowVehicleNotFoundExceptionWhenVehicleNotExists() {
        CreateServiceOrderRequest request = new CreateServiceOrderRequest(
                "Service Description Test",
                UUID.randomUUID()
        );
        TorqueDeskPrincipal principal = mock(TorqueDeskPrincipal.class);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(vehicleRepository.findById(request.vehicleId())).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> serviceOrderService.create(request);

        assertThatThrownBy(action).isInstanceOf(VehicleNotFoundException.class).hasMessage("Vehicle not found");
        verify(serviceOrderRepository, never()).save(any(ServiceOrder.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserIdNotExists() {
        CreateServiceOrderRequest request = new CreateServiceOrderRequest(
                "Service Description Test",
                UUID.randomUUID()
        );
        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .build();
        Vehicle vehicle = Vehicle.builder()
                .id(UUID.randomUUID())
                .brand("Test Brand")
                .model("Test Model")
                .modelYear(2020)
                .plate("Test Plate")
                .customer(customer)
                .build();
        TorqueDeskPrincipal principal = mock(TorqueDeskPrincipal.class);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(vehicleRepository.findById(request.vehicleId())).thenReturn(Optional.of(vehicle));
        when(userRepository.findById(principal.getUserId())).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> serviceOrderService.create(request);

        assertThatThrownBy(action).isInstanceOf(UserNotFoundException.class).hasMessage("User not found");
        verify(serviceOrderRepository, never()).save(any(ServiceOrder.class));
        verify(serviceOrderMapper, never()).toResponse(any(ServiceOrder.class));
    }

    @Test
    void shouldCreateServiceOrderAssignmentWhenServiceOrderExists() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .build();
        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .build();
        Vehicle vehicle = Vehicle.builder()
                .id(UUID.randomUUID())
                .model("Test Model")
                .build();
        CreateServiceOrderRequest request = new CreateServiceOrderRequest(
                "Service Description Test",
                vehicle.getId()
        );
        ServiceOrder serviceOrder = ServiceOrder.builder()
                .id(UUID.randomUUID())
                .build();
        ServiceOrderResponse expectedResponse = new ServiceOrderResponse(
                serviceOrder.getId(),
                vehicle.getId(),
                customer.getName(),
                vehicle.getModel(),
                request.serviceDescription()
        );
        TorqueDeskPrincipal principal = mock(TorqueDeskPrincipal.class);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(serviceOrderRepository.findById(serviceOrder.getId())).thenReturn(Optional.of(serviceOrder));
        when(userRepository.findById(principal.getUserId())).thenReturn(Optional.of(user));
        when(assignmentRepository.existsByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(serviceOrder.getId(), principal.getUserId())).thenReturn(false);
        when(serviceOrderMapper.toResponse(any(ServiceOrder.class))).thenReturn(expectedResponse);

        serviceOrderService.startAssignment(serviceOrder.getId());

        ArgumentCaptor<ServiceOrderAssignment> assignmentCaptor = ArgumentCaptor.forClass(ServiceOrderAssignment.class);
        verify(assignmentRepository).save(assignmentCaptor.capture());
        ServiceOrderAssignment savedAssignment = assignmentCaptor.getValue();
        assertThat(savedAssignment.getServiceOrder()).isEqualTo(serviceOrder);
        assertThat(savedAssignment.getEmployee()).isEqualTo(user);
        assertThat(savedAssignment.getStartedAt()).isNotNull();
    }

    @Test
    void shouldThrowServiceOrderNotFoundExceptionWhenServiceOrderNotExists() {
        UUID serviceOrderId = UUID.randomUUID();
        TorqueDeskPrincipal principal = mock(TorqueDeskPrincipal.class);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(serviceOrderRepository.findById(serviceOrderId)).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> serviceOrderService.startAssignment(serviceOrderId);

        assertThatThrownBy(action).isInstanceOf(ServiceOrderNotFoundException.class).hasMessage("Service order not found");
        verify(assignmentRepository, never()).save(any(ServiceOrderAssignment.class));
        verify(serviceOrderMapper, never()).toResponse(any(ServiceOrder.class));
    }

    @Test
    void shouldThrowServiceOrderAssignmentAlreadyStartedExceptionWhenAssignmentAlreadyStarted() {
        ServiceOrder serviceOrder = ServiceOrder.builder()
                .id(UUID.randomUUID())
                .build();
        TorqueDeskPrincipal principal = mock(TorqueDeskPrincipal.class);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(serviceOrderRepository.findById(serviceOrder.getId())).thenReturn(Optional.of(serviceOrder));
        when(assignmentRepository.existsByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(serviceOrder.getId(), principal.getUserId())).thenReturn(true);

        ThrowableAssert.ThrowingCallable action = () -> serviceOrderService.startAssignment(serviceOrder.getId());

        assertThatThrownBy(action).isInstanceOf(ServiceOrderAssignmentAlreadyStartedException.class).hasMessage("Assignment already started");
        verify(serviceOrderRepository, never()).save(any(ServiceOrder.class));
        verify(serviceOrderMapper, never()).toResponse(any(ServiceOrder.class));
    }

    @Test
    void shouldFinishAssignmentWhenServiceOrderExists() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        ServiceOrder serviceOrder = ServiceOrder.builder()
                .id(UUID.randomUUID())
                .build();
        ServiceOrderAssignment serviceOrderAssignment = ServiceOrderAssignment.builder()
                .id(UUID.randomUUID())
                .serviceOrder(serviceOrder)
                .employee(user)
                .startedAt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .finishedAt(null)
                .build();
        TorqueDeskPrincipal principal = mock(TorqueDeskPrincipal.class);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(assignmentRepository.findByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(serviceOrder.getId(), principal.getUserId()))
                .thenReturn(Optional.of(serviceOrderAssignment));

        serviceOrderService.finishAssignment(serviceOrder.getId());

        assertThat(serviceOrderAssignment.getFinishedAt()).isNotNull();
    }

    @Test
    void shouldThrowServiceOrderNotFoundExceptionWhenAssignmentAlreadyFinished() {
        ServiceOrder serviceOrder = ServiceOrder.builder()
                .id(UUID.randomUUID())
                .build();
        TorqueDeskPrincipal principal = mock(TorqueDeskPrincipal.class);
        when(securityUtils.getPrincipal()).thenReturn(principal);
        when(assignmentRepository.findByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(serviceOrder.getId(), principal.getUserId())).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> serviceOrderService.finishAssignment(serviceOrder.getId());

        assertThatThrownBy(action).isInstanceOf(ServiceOrderNotFoundException.class).hasMessage("ServiceOrder not found");
    }
}