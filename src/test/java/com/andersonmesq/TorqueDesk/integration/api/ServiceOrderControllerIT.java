package com.andersonmesq.TorqueDesk.integration.api;

import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import com.andersonmesq.TorqueDesk.security.principal.TorqueDeskPrincipal;
import com.andersonmesq.TorqueDesk.security.principal.UserPrincipal;
import com.andersonmesq.TorqueDesk.security.principal.WorkspacePrincipal;
import com.andersonmesq.TorqueDesk.service_order.dto.CreateServiceOrderRequest;
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
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import com.andersonmesq.TorqueDesk.usertenant.role.Role;
import com.andersonmesq.TorqueDesk.vehicle.model.Vehicle;
import com.andersonmesq.TorqueDesk.vehicle.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ServiceOrderControllerIT {

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
    private UserTenantRepository userTenantRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void cleanUp() {
        assignmentRepository.deleteAll();
        serviceOrderRepository.deleteAll();
        userTenantRepository.deleteAll();
        vehicleRepository.deleteAll();
        customerRepository.deleteAll();
        tenantRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateServiceOrderSuccessfully() throws Exception {
        UserTenant membership = createMembership(Role.MECHANIC);
        Vehicle vehicle = createVehicle();
        CreateServiceOrderRequest request = new CreateServiceOrderRequest("Inspect engine", vehicle.getId());

        mockMvc.perform(post("/api/v1/services")
                        .with(authenticationFor(membership))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serviceId").isNotEmpty())
                .andExpect(jsonPath("$.vehicleId").value(vehicle.getId().toString()))
                .andExpect(jsonPath("$.customerName").value(vehicle.getCustomer().getName()))
                .andExpect(jsonPath("$.vehicleName").value(vehicle.getModel()))
                .andExpect(jsonPath("$.serviceName").value(request.serviceDescription()));

        assertThat(serviceOrderRepository.findAll()).hasSize(1)
                .first().satisfies(order -> {
                    assertThat(order.getDescription()).isEqualTo(request.serviceDescription());
                    assertThat(order.getStatus()).isEqualTo(ServiceOrderStatus.OPEN);
                    assertThat(order.getVehicle().getId()).isEqualTo(vehicle.getId());
                });
    }

    @Test
    void shouldRejectServiceOrderCreationWhenVehicleDoesNotExist() throws Exception {
        UserTenant membership = createMembership(Role.OWNER);
        CreateServiceOrderRequest request = new CreateServiceOrderRequest("Inspect engine", UUID.randomUUID());

        mockMvc.perform(post("/api/v1/services")
                        .with(authenticationFor(membership))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        assertThat(serviceOrderRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectServiceOrderCreationWhenRequestIsInvalid() throws Exception {
        UserTenant membership = createMembership(Role.OWNER);
        CreateServiceOrderRequest request = new CreateServiceOrderRequest("", UUID.randomUUID());

        mockMvc.perform(post("/api/v1/services")
                        .with(authenticationFor(membership))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(serviceOrderRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectServiceOrderCreationWhenUnauthenticated() throws Exception {
        CreateServiceOrderRequest request = new CreateServiceOrderRequest("Inspect engine", UUID.randomUUID());

        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldStartAssignmentSuccessfully() throws Exception {
        UserTenant membership = createMembership(Role.MECHANIC);
        ServiceOrder serviceOrder = createServiceOrder(createVehicle(), membership.getUser());

        mockMvc.perform(post("/api/v1/services/{id}/assignments/start", serviceOrder.getId())
                        .with(authenticationFor(membership)))
                .andExpect(status().isOk());

        assertThat(assignmentRepository.findByServiceOrderIdAndEmployeeIdAndFinishedAtIsNull(
                serviceOrder.getId(), membership.getUser().getId())).isPresent();
    }

    @Test
    void shouldRejectStartingAnAlreadyActiveAssignment() throws Exception {
        UserTenant membership = createMembership(Role.MECHANIC);
        ServiceOrder serviceOrder = createServiceOrder(createVehicle(), membership.getUser());
        assignmentRepository.saveAndFlush(createAssignment(serviceOrder, membership.getUser()));

        mockMvc.perform(post("/api/v1/services/{id}/assignments/start", serviceOrder.getId())
                        .with(authenticationFor(membership)))
                .andExpect(status().isConflict());

        assertThat(assignmentRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldRejectStartingAssignmentWhenOrderDoesNotExist() throws Exception {
        UserTenant membership = createMembership(Role.MECHANIC);

        mockMvc.perform(post("/api/v1/services/{id}/assignments/start", UUID.randomUUID())
                        .with(authenticationFor(membership)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectStartingAssignmentForUnauthorizedRoleOrUnauthenticatedUser() throws Exception {
        UserTenant attendant = createMembership(Role.ATTENDANT);
        mockMvc.perform(post("/api/v1/services/{id}/assignments/start", UUID.randomUUID())
                        .with(authenticationFor(attendant)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/services/{id}/assignments/start", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFinishAssignmentSuccessfully() throws Exception {
        UserTenant membership = createMembership(Role.MECHANIC);
        ServiceOrder serviceOrder = createServiceOrder(createVehicle(), membership.getUser());
        ServiceOrderAssignment assignment = assignmentRepository.saveAndFlush(
                createAssignment(serviceOrder, membership.getUser()));

        mockMvc.perform(patch("/api/v1/services/{id}/assignments/finish", serviceOrder.getId())
                        .with(authenticationFor(membership)))
                .andExpect(status().isNoContent());

        assertThat(assignmentRepository.findById(assignment.getId()).orElseThrow().getFinishedAt()).isNotNull();
    }

    @Test
    void shouldRejectFinishingAssignmentWhenNoActiveAssignmentExists() throws Exception {
        UserTenant membership = createMembership(Role.MECHANIC);

        mockMvc.perform(patch("/api/v1/services/{id}/assignments/finish", UUID.randomUUID())
                        .with(authenticationFor(membership)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectFinishingAssignmentForUnauthorizedRoleOrUnauthenticatedUser() throws Exception {
        UserTenant attendant = createMembership(Role.ATTENDANT);
        mockMvc.perform(patch("/api/v1/services/{id}/assignments/finish", UUID.randomUUID())
                        .with(authenticationFor(attendant)))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/services/{id}/assignments/finish", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    private UserTenant createMembership(Role role) {
        User user = userRepository.saveAndFlush(User.builder()
                .systemRole(SystemRole.USER)
                .fullName("Service Order API User")
                .userName("service-order-api-" + UUID.randomUUID())
                .email("service-order-api-" + UUID.randomUUID() + "@example.com")
                .password("encoded-password")
                .enabled(true)
                .build());
        Tenant tenant = tenantRepository.saveAndFlush(Tenant.builder()
                .name("Service Order API Tenant")
                .slug("service-order-api-" + UUID.randomUUID())
                .status(TenantStatus.ACTIVE)
                .build());
        return userTenantRepository.saveAndFlush(UserTenant.builder()
                .user(user)
                .tenant(tenant)
                .role(role)
                .enabled(true)
                .build());
    }

    private Vehicle createVehicle() {
        Tenant tenant = tenantRepository.saveAndFlush(Tenant.builder()
                .name("Service Order Vehicle Tenant")
                .slug("service-order-vehicle-" + UUID.randomUUID())
                .status(TenantStatus.ACTIVE)
                .build());
        Customer customer = customerRepository.saveAndFlush(Customer.builder()
                .name("Service Order API Customer")
                .email("service-order-customer-" + UUID.randomUUID() + "@example.com")
                .phone("service-order-customer-" + UUID.randomUUID())
                .tenant(tenant)
                .build());
        return vehicleRepository.saveAndFlush(Vehicle.builder()
                .brand("Honda")
                .model("Civic")
                .modelYear(2022)
                .plate("service-order-api-" + UUID.randomUUID())
                .customer(customer)
                .build());
    }

    private ServiceOrder createServiceOrder(Vehicle vehicle, User creator) {
        return serviceOrderRepository.saveAndFlush(ServiceOrder.builder()
                .description("Inspect engine")
                .status(ServiceOrderStatus.OPEN)
                .vehicle(vehicle)
                .createdBy(creator)
                .build());
    }

    private ServiceOrderAssignment createAssignment(ServiceOrder order, User employee) {
        return ServiceOrderAssignment.builder()
                .serviceOrder(order)
                .employee(employee)
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .build();
    }

    private RequestPostProcessor authenticationFor(UserTenant membership) {
        UserPrincipal userPrincipal = UserPrincipal.create(membership.getUser());
        WorkspacePrincipal workspacePrincipal = WorkspacePrincipal.create(membership);
        TorqueDeskPrincipal principal = TorqueDeskPrincipal.builder()
                .userPrincipal(userPrincipal)
                .workspacePrincipal(workspacePrincipal)
                .build();
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
    }
}