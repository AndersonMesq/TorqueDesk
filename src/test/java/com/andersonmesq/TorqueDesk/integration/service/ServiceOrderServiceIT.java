package com.andersonmesq.TorqueDesk.integration.service;

import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import com.andersonmesq.TorqueDesk.security.principal.TorqueDeskPrincipal;
import com.andersonmesq.TorqueDesk.service_order.dto.CreateServiceOrderRequest;
import com.andersonmesq.TorqueDesk.service_order.exception.ServiceOrderAssignmentAlreadyStartedException;
import com.andersonmesq.TorqueDesk.service_order.exception.ServiceOrderNotFoundException;
import com.andersonmesq.TorqueDesk.service_order.model.ServiceOrder;
import com.andersonmesq.TorqueDesk.service_order.model.ServiceOrderAssignment;
import com.andersonmesq.TorqueDesk.service_order.repository.ServiceOrderAssignmentRepository;
import com.andersonmesq.TorqueDesk.service_order.repository.ServiceOrderRepository;
import com.andersonmesq.TorqueDesk.service_order.service.ServiceOrderService;
import com.andersonmesq.TorqueDesk.service_order.service_order_status.ServiceOrderStatus;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
import com.andersonmesq.TorqueDesk.vehicle.exception.VehicleNotFoundException;
import com.andersonmesq.TorqueDesk.vehicle.model.Vehicle;
import com.andersonmesq.TorqueDesk.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ServiceOrderServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private ServiceOrderService serviceOrderService;

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    @Autowired
    private ServiceOrderAssignmentRepository assignmentRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        assignmentRepository.deleteAll();
        serviceOrderRepository.deleteAll();
        vehicleRepository.deleteAll();
        customerRepository.deleteAll();
        tenantRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateServiceOrderForAuthenticatedUser() {
        User creator = createUser("creator");
        Vehicle vehicle = createVehicle();
        authenticateAs(creator);

        var response = serviceOrderService.create(
                new CreateServiceOrderRequest("Replace brake pads", vehicle.getId()));

        ServiceOrder persisted = serviceOrderRepository.findById(response.serviceId()).orElseThrow();
        assertThat(response.vehicleId()).isEqualTo(vehicle.getId());
        assertThat(response.customerName()).isEqualTo(vehicle.getCustomer().getName());
        assertThat(response.vehicleName()).isEqualTo(vehicle.getModel());
        assertThat(response.serviceName()).isEqualTo("Replace brake pads");
        assertThat(persisted.getDescription()).isEqualTo("Replace brake pads");
        assertThat(persisted.getStatus()).isEqualTo(ServiceOrderStatus.OPEN);
        assertThat(persisted.getVehicle().getId()).isEqualTo(vehicle.getId());
        assertThat(persisted.getCreatedBy().getId()).isEqualTo(creator.getId());
    }

    @Test
    void shouldRejectServiceOrderCreationWhenVehicleDoesNotExist() {
        User creator = createUser("creator");
        authenticateAs(creator);

        assertThatThrownBy(() -> serviceOrderService.create(
                new CreateServiceOrderRequest("Inspect vehicle", UUID.randomUUID())))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle not found");

        assertThat(serviceOrderRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectServiceOrderCreationWhenAuthenticatedUserDoesNotExist() {
        User missingUser = User.builder()
                .id(UUID.randomUUID())
                .systemRole(SystemRole.USER)
                .fullName("Missing User")
                .build();
        authenticateAs(missingUser);
        Vehicle vehicle = createVehicle();

        assertThatThrownBy(() -> serviceOrderService.create(
                new CreateServiceOrderRequest("Inspect vehicle", vehicle.getId())))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        assertThat(serviceOrderRepository.findAll()).isEmpty();
    }

    @Test
    void shouldStartAssignmentSuccessfully() {
        User employee = createUser("employee");
        ServiceOrder serviceOrder = createServiceOrder(employee);
        authenticateAs(employee);

        serviceOrderService.startAssignment(serviceOrder.getId());

        ServiceOrderAssignment assignment = assignmentRepository
                .findByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(serviceOrder.getId(), employee.getId())
                .orElseThrow();
        assertThat(assignment.getStartedAt()).isNotNull();
        assertThat(assignment.getFinishedAt()).isNull();
        assertThat(assignment.getServiceOrder().getId()).isEqualTo(serviceOrder.getId());
        assertThat(assignment.getEmployee().getId()).isEqualTo(employee.getId());
    }

    @Test
    void shouldRejectStartingAssignmentWhenItIsAlreadyActive() {
        User employee = createUser("employee");
        ServiceOrder serviceOrder = createServiceOrder(employee);
        assignmentRepository.saveAndFlush(createAssignment(serviceOrder, employee));
        authenticateAs(employee);

        assertThatThrownBy(() -> serviceOrderService.startAssignment(serviceOrder.getId()))
                .isInstanceOf(ServiceOrderAssignmentAlreadyStartedException.class)
                .hasMessage("Assignment already started");

        assertThat(assignmentRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldRejectStartingAssignmentWhenOrderDoesNotExist() {
        User employee = createUser("employee");
        authenticateAs(employee);

        assertThatThrownBy(() -> serviceOrderService.startAssignment(UUID.randomUUID()))
                .isInstanceOf(ServiceOrderNotFoundException.class)
                .hasMessage("Service order not found");

        assertThat(assignmentRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectStartingAssignmentWhenAuthenticatedUserDoesNotExist() {
        User missingEmployee = User.builder()
                .id(UUID.randomUUID())
                .systemRole(SystemRole.USER)
                .fullName("Missing Employee")
                .build();
        ServiceOrder serviceOrder = createServiceOrder(createUser("creator"));
        authenticateAs(missingEmployee);

        assertThatThrownBy(() -> serviceOrderService.startAssignment(serviceOrder.getId()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        assertThat(assignmentRepository.findAll()).isEmpty();
    }

    @Test
    void shouldFinishActiveAssignmentSuccessfully() {
        User employee = createUser("employee");
        ServiceOrder serviceOrder = createServiceOrder(employee);
        ServiceOrderAssignment assignment = assignmentRepository.saveAndFlush(
                createAssignment(serviceOrder, employee));
        authenticateAs(employee);

        serviceOrderService.finishAssignment(serviceOrder.getId());

        assertThat(assignmentRepository.findById(assignment.getId()).orElseThrow().getFinishedAt()).isNotNull();
        assertThat(assignmentRepository.findByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(
                serviceOrder.getId(), employee.getId())).isEmpty();
    }

    @Test
    void shouldRejectFinishingAssignmentWhenNoActiveAssignmentExists() {
        User employee = createUser("employee");
        authenticateAs(employee);

        assertThatThrownBy(() -> serviceOrderService.finishAssignment(UUID.randomUUID()))
                .isInstanceOf(ServiceOrderNotFoundException.class)
                .hasMessage("ServiceOrder not found");

        assertThat(assignmentRepository.findAll()).isEmpty();
    }

    private User createUser(String label) {
        return userRepository.saveAndFlush(User.builder()
                .systemRole(SystemRole.USER)
                .fullName("Service Order " + label)
                .userName(label + "-" + UUID.randomUUID())
                .email(label + "-" + UUID.randomUUID() + "@example.com")
                .password("encoded-password")
                .enabled(true)
                .build());
    }

    private Vehicle createVehicle() {
        Tenant tenant = tenantRepository.saveAndFlush(Tenant.builder()
                .name("Service Order Tenant")
                .slug("service-order-" + UUID.randomUUID())
                .status(TenantStatus.ACTIVE)
                .build());
        Customer customer = customerRepository.saveAndFlush(Customer.builder()
                .name("Service Order Customer")
                .email("service-order-" + UUID.randomUUID() + "@example.com")
                .phone("service-order-" + UUID.randomUUID())
                .tenant(tenant)
                .build());
        return vehicleRepository.saveAndFlush(Vehicle.builder()
                .brand("Honda")
                .model("Civic")
                .modelYear(2022)
                .plate("service-order-" + UUID.randomUUID())
                .customer(customer)
                .build());
    }

    private ServiceOrder createServiceOrder(User creator) {
        return serviceOrderRepository.saveAndFlush(ServiceOrder.builder()
                .description("Inspect engine")
                .status(ServiceOrderStatus.OPEN)
                .vehicle(createVehicle())
                .createdBy(creator)
                .build());
    }

    private ServiceOrderAssignment createAssignment(ServiceOrder serviceOrder, User employee) {
        return ServiceOrderAssignment.builder()
                .serviceOrder(serviceOrder)
                .employee(employee)
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .build();
    }

    private void authenticateAs(User user) {
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.fromUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
