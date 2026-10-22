package com.andersonmesq.TorqueDesk.integration.repository;

import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import com.andersonmesq.TorqueDesk.service_order.model.ServiceOrder;
import com.andersonmesq.TorqueDesk.service_order.model.ServiceOrderAssignment;
import com.andersonmesq.TorqueDesk.service_order.repository.ServiceOrderAssignmentRepository;
import com.andersonmesq.TorqueDesk.service_order.repository.ServiceOrderRepository;
import com.andersonmesq.TorqueDesk.service_order.service_order_status.ServiceOrderStatus;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
import com.andersonmesq.TorqueDesk.vehicle.model.Vehicle;
import com.andersonmesq.TorqueDesk.vehicle.repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ServiceOrderRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

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

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndFindServiceOrderById() {
        User creator = createUser("creator");
        Vehicle vehicle = createVehicle();
        ServiceOrder serviceOrder = serviceOrderRepository.save(createServiceOrder(vehicle, creator));
        entityManager.flush();
        entityManager.clear();

        ServiceOrder persisted = serviceOrderRepository.findById(serviceOrder.getId()).orElseThrow();

        assertThat(persisted.getDescription()).isEqualTo(serviceOrder.getDescription());
        assertThat(persisted.getStatus()).isEqualTo(ServiceOrderStatus.OPEN);
        assertThat(persisted.getVehicle().getId()).isEqualTo(vehicle.getId());
        assertThat(persisted.getCreatedBy().getId()).isEqualTo(creator.getId());
    }

    @Test
    void shouldFindActiveAssignmentByServiceOrderAndEmployee() {
        User employee = createUser("employee");
        ServiceOrder serviceOrder = serviceOrderRepository.saveAndFlush(
                createServiceOrder(createVehicle(), createUser("creator")));
        ServiceOrderAssignment assignment = assignmentRepository.saveAndFlush(
                createAssignment(serviceOrder, employee));
        entityManager.clear();

        assertThat(assignmentRepository.findByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(
                serviceOrder.getId(), employee.getId()))
                .isPresent().get().satisfies(found -> assertThat(found.getId()).isEqualTo(assignment.getId()));
        assertThat(assignmentRepository.findByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(
                serviceOrder.getId(), UUID.randomUUID())).isEmpty();
    }

    @Test
    void shouldCheckWhetherAnActiveAssignmentExists() {
        User employee = createUser("employee");
        ServiceOrder serviceOrder = serviceOrderRepository.saveAndFlush(
                createServiceOrder(createVehicle(), createUser("creator")));
        ServiceOrderAssignment assignment = assignmentRepository.saveAndFlush(
                createAssignment(serviceOrder, employee));
        entityManager.clear();

        assertThat(assignmentRepository.existsByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(
                serviceOrder.getId(), employee.getId())).isTrue();
        assertThat(assignmentRepository.existsByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(
                serviceOrder.getId(), UUID.randomUUID())).isFalse();

        ServiceOrderAssignment persistedAssignment = assignmentRepository.findById(assignment.getId()).orElseThrow();
        persistedAssignment.setFinishedAt(LocalDateTime.now());
        assignmentRepository.flush();

        assertThat(assignmentRepository.existsByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(
                serviceOrder.getId(), employee.getId())).isFalse();
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

    private ServiceOrder createServiceOrder(Vehicle vehicle, User creator) {
        return ServiceOrder.builder()
                .description("Inspect engine")
                .status(ServiceOrderStatus.OPEN)
                .vehicle(vehicle)
                .createdBy(creator)
                .build();
    }

    private ServiceOrderAssignment createAssignment(ServiceOrder serviceOrder, User employee) {
        return ServiceOrderAssignment.builder()
                .serviceOrder(serviceOrder)
                .employee(employee)
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .build();
    }
}
