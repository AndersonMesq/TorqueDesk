package com.andersonmesq.TorqueDesk.integration.service;

import com.andersonmesq.TorqueDesk.customer.exception.CustomerNotFoundException;
import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import com.andersonmesq.TorqueDesk.vehicle.dto.CreateVehicleRequest;
import com.andersonmesq.TorqueDesk.vehicle.dto.UpdateVehicleRequest;
import com.andersonmesq.TorqueDesk.vehicle.exception.VehicleAlreadyExistException;
import com.andersonmesq.TorqueDesk.vehicle.exception.VehicleNotFoundException;
import com.andersonmesq.TorqueDesk.vehicle.model.Vehicle;
import com.andersonmesq.TorqueDesk.vehicle.repository.VehicleRepository;
import com.andersonmesq.TorqueDesk.vehicle.service.VehicleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class VehicleServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @AfterEach
    void cleanUp() {
        vehicleRepository.deleteAll();
        customerRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void shouldCreateVehicleSuccessfully() {
        Customer customer = createCustomer();
        CreateVehicleRequest request = createRequest(customer.getId(), "ABC-123");

        var response = vehicleService.createVehicle(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.brand()).isEqualTo(request.brand());
        assertThat(response.model()).isEqualTo(request.model());
        assertThat(response.plate()).isEqualTo(request.plate());
        assertThat(vehicleRepository.findById(response.id()).orElseThrow().getCustomer().getId())
                .isEqualTo(customer.getId());
    }

    @Test
    void shouldRejectVehicleCreationWhenVehicleAlreadyExists() {
        Customer customer = createCustomer();
        CreateVehicleRequest request = createRequest(customer.getId(), "ABC-123");
        vehicleService.createVehicle(request);

        assertThatThrownBy(() -> vehicleService.createVehicle(request))
                .isInstanceOf(VehicleAlreadyExistException.class)
                .hasMessage("Vehicle already exists");

        assertThat(vehicleRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldRejectVehicleCreationWhenCustomerDoesNotExist() {
        assertThatThrownBy(() -> vehicleService.createVehicle(
                createRequest(UUID.randomUUID(), "ABC-123")))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer not found");

        assertThat(vehicleRepository.findAll()).isEmpty();
    }

    @Test
    void shouldFindVehicleByIdSuccessfully() {
        Vehicle vehicle = createVehicle(createCustomer(), "ABC-123");

        var response = vehicleService.findById(vehicle.getId());

        assertThat(response.id()).isEqualTo(vehicle.getId());
        assertThat(response.plate()).isEqualTo(vehicle.getPlate());
    }

    @Test
    void shouldRejectFindByIdWhenVehicleDoesNotExist() {
        assertThatThrownBy(() -> vehicleService.findById(UUID.randomUUID()))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle not found");
    }

    @Test
    void shouldFindVehicleByPlateSuccessfully() {
        Vehicle vehicle = createVehicle(createCustomer(), "ABC-123");

        var response = vehicleService.findByPlate(vehicle.getPlate());

        assertThat(response.id()).isEqualTo(vehicle.getId());
        assertThat(response.plate()).isEqualTo(vehicle.getPlate());
    }

    @Test
    void shouldRejectFindByPlateWhenVehicleDoesNotExist() {
        assertThatThrownBy(() -> vehicleService.findByPlate("missing-plate"))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle not found");
    }

    @Test
    void shouldFindVehicleByModelSuccessfully() {
        Vehicle vehicle = createVehicle(createCustomer(), "ABC-123");

        var response = vehicleService.findByModel(vehicle.getModel());

        assertThat(response.id()).isEqualTo(vehicle.getId());
        assertThat(response.model()).isEqualTo(vehicle.getModel());
    }

    @Test
    void shouldRejectFindByModelWhenVehicleDoesNotExist() {
        assertThatThrownBy(() -> vehicleService.findByModel("missing-model"))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle not found");
    }

    @Test
    void shouldUpdateVehicleSuccessfully() {
        Vehicle vehicle = createVehicle(createCustomer(), "ABC-123");

        var response = vehicleService.updateVehicle(vehicle.getId(), new UpdateVehicleRequest("XYZ-789"));

        assertThat(response.id()).isEqualTo(vehicle.getId());
        assertThat(response.plate()).isEqualTo("XYZ-789");
        assertThat(vehicleRepository.findById(vehicle.getId()).orElseThrow().getPlate()).isEqualTo("XYZ-789");
    }

    @Test
    void shouldRejectVehicleUpdateWhenVehicleDoesNotExist() {
        assertThatThrownBy(() -> vehicleService.updateVehicle(
                UUID.randomUUID(), new UpdateVehicleRequest("XYZ-789")))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle not found");
    }

    private Customer createCustomer() {
        Tenant tenant = tenantRepository.saveAndFlush(Tenant.builder()
                .name("Vehicle Service Tenant")
                .slug("vehicle-service-" + UUID.randomUUID())
                .status(TenantStatus.ACTIVE)
                .build());
        return customerRepository.saveAndFlush(Customer.builder()
                .name("Vehicle Service Customer")
                .email("vehicle-service-" + UUID.randomUUID() + "@example.com")
                .phone("vehicle-service-" + UUID.randomUUID())
                .tenant(tenant)
                .build());
    }

    private Vehicle createVehicle(Customer customer, String plate) {
        return vehicleRepository.saveAndFlush(Vehicle.builder()
                .brand("Honda")
                .model("Civic")
                .modelYear(2022)
                .plate(plate)
                .customer(customer)
                .build());
    }

    private CreateVehicleRequest createRequest(UUID customerId, String plate) {
        return new CreateVehicleRequest(customerId, "Civic", "Honda", "Civic", 2022, plate);
    }
}
