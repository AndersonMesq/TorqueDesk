package com.andersonmesq.TorqueDesk.integration.service;

import com.andersonmesq.TorqueDesk.customer.dto.CreateCustomerRequest;
import com.andersonmesq.TorqueDesk.customer.dto.CustomerResponse;
import com.andersonmesq.TorqueDesk.customer.exception.CustomerAlreadyExistException;
import com.andersonmesq.TorqueDesk.customer.exception.CustomerNotFoundException;
import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import com.andersonmesq.TorqueDesk.customer.service.CustomerService;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantNotFoundException;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class CustomerServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @AfterEach
    void tearDown() {
        customerRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void shouldCreateCustomerSuccessfully(){
        Tenant tenant = createAndSaveTenant();
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John Test",
                "john.test@email.com",
                "85999999999",
                tenant.getId()
        );

        CustomerResponse response = customerService.create(request);

        assertThat(response).isNotNull().satisfies(customer -> {
            assertThat(customer.id()).isNotNull();
            assertThat(customer.name()).isEqualTo(request.name());
            assertThat(customer.email()).isEqualTo(request.email());
        });
        Customer persistedCustomer = customerRepository.findById(response.id()).orElseThrow();
        assertThat(persistedCustomer.getName()).isEqualTo(request.name());
        assertThat(persistedCustomer.getEmail()).isEqualTo(request.email());
        assertThat(persistedCustomer.getPhone()).isEqualTo(request.phone());
        assertThat(persistedCustomer.getTenant().getId()).isEqualTo(tenant.getId());
    }

    @Test
    void shouldRejectCreationWhenEmailAlreadyExists(){
        Tenant tenant = createAndSaveTenant();
        CreateCustomerRequest firstRequest = new CreateCustomerRequest(
                "John Test",
                "john.test@email.com",
                "85999999999",
                tenant.getId()
        );
        customerService.create(firstRequest);
        CreateCustomerRequest duplicatedRequest = new CreateCustomerRequest(
                "John Test",
                "john.test@email.com",
                "85999999998",
                tenant.getId()
        );

        assertThatThrownBy(() -> customerService.create(duplicatedRequest))
                .isInstanceOf(CustomerAlreadyExistException.class)
                .hasMessage("Customer already exist");
        assertThat(customerRepository.findAll()).hasSize(1);
        assertThat(customerRepository.findByEmail(firstRequest.email()))
                .isPresent()
                .get()
                .extracting(Customer::getName)
                .isEqualTo(firstRequest.name());
    }

    @Test
    void shouldRejectCreationWhenTenantDoesNotExists(){
        UUID nonExistentTenantId = UUID.randomUUID();
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John Test",
                "john.test@email.com",
                "85999999999",
                nonExistentTenantId
        );

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(TenantNotFoundException.class)
                .hasMessage("Tenant not found");
        assertThat(customerRepository.findAll().isEmpty());
    }

    @Test
    void shouldFindCustomerByIdSuccessfully(){
        Tenant tenant = createAndSaveTenant();
        Customer customer = createAndSaveCustomer(tenant);

        CustomerResponse response = customerService.findById(customer.getId());

        assertThat(response).isNotNull().satisfies(result -> {
           assertThat(result.id()).isEqualTo(customer.getId());
           assertThat(result.name()).isEqualTo(customer.getName());
           assertThat(result.email()).isEqualTo(customer.getEmail());
        });
    }

    @Test
    void shouldRejectFindByIdWhenCustomerDoesNotExist(){
        UUID nonExistentCustomerId = UUID.randomUUID();

        assertThatThrownBy(() -> customerService.findById(nonExistentCustomerId))
                .isInstanceOf(CustomerNotFoundException.class).hasMessage("Customer not found");
    }

    @Test
    void shouldFindCustomerByEmailSuccessfully(){
        Tenant tenant = createAndSaveTenant();
        Customer customer = createAndSaveCustomer(tenant);

        CustomerResponse response = customerService.findByEmail(customer.getEmail());

        assertThat(response).isNotNull().satisfies(result -> {
            assertThat(result.id()).isEqualTo(customer.getId());
            assertThat(result.name()).isEqualTo(customer.getName());
            assertThat(result.email()).isEqualTo(customer.getEmail());
        });
    }

    @Test
    void shouldRejectFindByEmailWhenCustomerDoesNotExists(){
        String nonExistentEmail = "nonexistentemail@email.com";

        assertThatThrownBy(() -> customerService.findByEmail(nonExistentEmail))
                .isInstanceOf(CustomerNotFoundException.class).hasMessage("Customer not found");
    }

    private Tenant createAndSaveTenant(){
        Tenant tenant = Tenant.builder()
                .name("Test Tenant")
                .slug("test-tenant")
                .status(TenantStatus.ACTIVE)
                .build();
        return tenantRepository.saveAndFlush(tenant);
    }

    private Customer createAndSaveCustomer(Tenant tenant){
        Customer customer = Customer.builder()
                .name("John test")
                .email("john.test@gmail.com")
                .phone("85999990001")
                .tenant(tenant)
                .build();
        return customerRepository.saveAndFlush(customer);
    }
}