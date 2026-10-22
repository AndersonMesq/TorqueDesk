package com.andersonmesq.TorqueDesk.integration.repository;

import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VehicleRepositoryIT {

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
    private EntityManager entityManager;

    @Test
    void shouldPersistVehicle() {
        Customer customer = createCustomer();
        Vehicle vehicle = vehicleRepository.save(createVehicle(customer));
        entityManager.flush();
        entityManager.clear();

        Vehicle persisted = vehicleRepository.findById(vehicle.getId()).orElseThrow();

        assertThat(persisted.getBrand()).isEqualTo(vehicle.getBrand());
        assertThat(persisted.getModel()).isEqualTo(vehicle.getModel());
        assertThat(persisted.getModelYear()).isEqualTo(vehicle.getModelYear());
        assertThat(persisted.getPlate()).isEqualTo(vehicle.getPlate());
        assertThat(persisted.getCustomer().getId()).isEqualTo(customer.getId());
    }

    @Test
    void shouldFindVehicleByPlateAndReturnEmptyWhenMissing() {
        Vehicle vehicle = vehicleRepository.saveAndFlush(createVehicle(createCustomer()));
        entityManager.clear();

        Optional<Vehicle> result = vehicleRepository.findByPlate(vehicle.getPlate());

        assertThat(result).isPresent().get()
                .satisfies(found -> assertThat(found.getId()).isEqualTo(vehicle.getId()));
        assertThat(vehicleRepository.findByPlate("missing-plate")).isEmpty();
    }

    @Test
    void shouldFindVehicleByModelAndReturnEmptyWhenMissing() {
        Vehicle vehicle = vehicleRepository.saveAndFlush(createVehicle(createCustomer()));
        entityManager.clear();

        Optional<Vehicle> result = vehicleRepository.findByModel(vehicle.getModel());

        assertThat(result).isPresent().get()
                .satisfies(found -> assertThat(found.getId()).isEqualTo(vehicle.getId()));
        assertThat(vehicleRepository.findByModel("Missing model")).isEmpty();
    }

    @Test
    void shouldCheckVehicleExistsById() {
        Vehicle vehicle = vehicleRepository.saveAndFlush(createVehicle(createCustomer()));
        entityManager.clear();

        assertThat(vehicleRepository.existsById(vehicle.getId())).isTrue();
        assertThat(vehicleRepository.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    void shouldCheckVehicleExistsByPlate() {
        Vehicle vehicle = vehicleRepository.saveAndFlush(createVehicle(createCustomer()));
        entityManager.clear();

        assertThat(vehicleRepository.existsByPlate(vehicle.getPlate())).isTrue();
        assertThat(vehicleRepository.existsByPlate("missing-plate")).isFalse();
    }

    @Test
    void shouldCheckVehicleExistsByPlateAndModel() {
        Vehicle vehicle = vehicleRepository.saveAndFlush(createVehicle(createCustomer()));
        entityManager.clear();

        assertThat(vehicleRepository.existsByPlateAndModel(vehicle.getPlate(), vehicle.getModel())).isTrue();
        assertThat(vehicleRepository.existsByPlateAndModel(vehicle.getPlate(), "Different model")).isFalse();
    }

    private Customer createCustomer() {
        Tenant tenant = tenantRepository.saveAndFlush(Tenant.builder()
                .name("Vehicle Test Tenant")
                .slug("vehicle-test-" + UUID.randomUUID())
                .status(TenantStatus.ACTIVE)
                .build());
        return customerRepository.saveAndFlush(Customer.builder()
                .name("Vehicle Test Customer")
                .email("vehicle-" + UUID.randomUUID() + "@example.com")
                .phone("phone-" + UUID.randomUUID())
                .tenant(tenant)
                .build());
    }

    private Vehicle createVehicle(Customer customer) {
        return Vehicle.builder()
                .brand("Subaru")
                .model("Legacy " + UUID.randomUUID())
                .modelYear(2020)
                .plate("plate-" + UUID.randomUUID())
                .customer(customer)
                .build();
    }
}
