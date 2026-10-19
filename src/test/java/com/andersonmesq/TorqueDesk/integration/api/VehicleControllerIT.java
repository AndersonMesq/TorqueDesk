package com.andersonmesq.TorqueDesk.integration.api;

import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import com.andersonmesq.TorqueDesk.vehicle.dto.CreateVehicleRequest;
import com.andersonmesq.TorqueDesk.vehicle.dto.UpdateVehicleRequest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class VehicleControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void cleanUp() {
        vehicleRepository.deleteAll();
        customerRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void shouldCreateVehicleSuccessfully() throws Exception {
        Customer customer = createCustomer();
        CreateVehicleRequest request = createRequest(customer, "ABC-123");

        mockMvc.perform(post("/api/v1/vehicles")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.brand").value(request.brand()))
                .andExpect(jsonPath("$.model").value(request.model()))
                .andExpect(jsonPath("$.plate").value(request.plate()));

        assertThat(vehicleRepository.findByPlate(request.plate())).isPresent();
    }

    @Test
    void shouldRejectVehicleCreationWhenRequestIsInvalid() throws Exception {
        Customer customer = createCustomer();
        CreateVehicleRequest request = new CreateVehicleRequest(
                customer.getId(), "", "Honda", "Civic", 2022, "ABC-123");

        mockMvc.perform(post("/api/v1/vehicles")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(vehicleRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectVehicleCreationWhenUnauthenticated() throws Exception {
        Customer customer = createCustomer();
        CreateVehicleRequest request = createRequest(customer, "ABC-123");

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFindVehicleByPlateSuccessfully() throws Exception {
        Vehicle vehicle = createVehicle(createCustomer(), "ABC-123", "Civic");

        mockMvc.perform(get("/api/v1/vehicles/by-plate/{plate}", vehicle.getPlate())
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicle.getId().toString()))
                .andExpect(jsonPath("$.plate").value(vehicle.getPlate()));
    }

    @Test
    void shouldReturnNotFoundWhenVehiclePlateDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/by-plate/{plate}", "missing-plate")
                        .with(authenticatedUser()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectFindByPlateWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/by-plate/{plate}", "ABC-123"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFindVehicleByModelSuccessfully() throws Exception {
        Vehicle vehicle = createVehicle(createCustomer(), "ABC-123", "Civic");

        mockMvc.perform(get("/api/v1/vehicles/by-model/{model}", vehicle.getModel())
                        .with(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicle.getId().toString()))
                .andExpect(jsonPath("$.model").value(vehicle.getModel()));
    }

    @Test
    void shouldReturnNotFoundWhenVehicleModelDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/by-model/{model}", "missing-model")
                        .with(authenticatedUser()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectFindByModelWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/by-model/{model}", "Civic"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldUpdateVehicleSuccessfully() throws Exception {
        Vehicle vehicle = createVehicle(createCustomer(), "ABC-123", "Civic");
        UpdateVehicleRequest request = new UpdateVehicleRequest("XYZ-789");

        mockMvc.perform(put("/api/v1/vehicles/{id}", vehicle.getId())
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value(request.plate()));

        assertThat(vehicleRepository.findById(vehicle.getId()).orElseThrow().getPlate())
                .isEqualTo(request.plate());
    }

    @Test
    void shouldRejectVehicleUpdateWhenVehicleDoesNotExist() throws Exception {
        mockMvc.perform(put("/api/v1/vehicles/{id}", UUID.randomUUID())
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateVehicleRequest("XYZ-789"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectVehicleUpdateWhenUnauthenticated() throws Exception {
        mockMvc.perform(put("/api/v1/vehicles/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateVehicleRequest("XYZ-789"))))
                .andExpect(status().isForbidden());
    }

    private Customer createCustomer() {
        Tenant tenant = tenantRepository.saveAndFlush(Tenant.builder()
                .name("Vehicle API Tenant")
                .slug("vehicle-api-" + UUID.randomUUID())
                .status(TenantStatus.ACTIVE)
                .build());
        return customerRepository.saveAndFlush(Customer.builder()
                .name("Vehicle API Customer")
                .email("vehicle-api-" + UUID.randomUUID() + "@example.com")
                .phone("vehicle-api-" + UUID.randomUUID())
                .tenant(tenant)
                .build());
    }

    private Vehicle createVehicle(Customer customer, String plate, String model) {
        return vehicleRepository.saveAndFlush(Vehicle.builder()
                .brand("Honda")
                .model(model)
                .modelYear(2022)
                .plate(plate)
                .customer(customer)
                .build());
    }

    private CreateVehicleRequest createRequest(Customer customer, String plate) {
        return new CreateVehicleRequest(customer.getId(), "Civic", "Honda", "Civic", 2022, plate);
    }

    private RequestPostProcessor authenticatedUser() {
        return user("test-user");
    }
}
