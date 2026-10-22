package com.andersonmesq.TorqueDesk.integration.api;

import com.andersonmesq.TorqueDesk.customer.dto.CreateCustomerRequest;
import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
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
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class CustomerControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void shouldCreateCustomerSuccessfully() throws Exception {
        Tenant tenant = createAndSaveTenant();
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John Test",
                "john.test@email.com",
                "85999999999",
                tenant.getId()
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/api/v1/customers")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
        );

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.email").value(request.email()));
        assertThat(customerRepository.findAll()).hasSize(1).first().satisfies(customer -> {
                    assertThat(customer.getName()).isEqualTo(request.name());
                    assertThat(customer.getEmail()).isEqualTo(request.email());
                    assertThat(customer.getPhone()).isEqualTo(request.phone());
                    assertThat(customer.getTenant().getId()).isEqualTo(tenant.getId());
                });
    }

    @Test
    void shouldRejectCreationWhenUserIsNotAuthenticated() throws Exception {
        Tenant tenant = createAndSaveTenant();
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John Test",
                "john.test@email.com",
                "85999999999",
                tenant.getId()
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
        );

        result.andExpect(status().isForbidden());
        assertThat(customerRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectCreationWhenRequestIsInvalid() throws Exception {
        Tenant tenant = createAndSaveTenant();
        CreateCustomerRequest request = new CreateCustomerRequest(
                "",
                "invalid-email",
                "",
                tenant.getId()
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(
                post("/api/v1/customers")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
        );

        result.andExpect(status().isBadRequest());
        assertThat(customerRepository.findAll()).isEmpty();
    }

    @Test
    void shouldFindCustomerByIdSuccessfully() throws Exception {
        Tenant tenant = createAndSaveTenant();
        Customer customer = createAndSaveCustomer(tenant);

        ResultActions result = mockMvc.perform(
                get("/api/v1/customers/{id}", customer.getId()).with(user("test-user"))
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId().toString()))
                .andExpect(jsonPath("$.name").value(customer.getName()))
                .andExpect(jsonPath("$.email").value(customer.getEmail()));
    }

    @Test
    void shouldFindCustomerByEmailSuccessfully() throws Exception {
        Tenant tenant = createAndSaveTenant();
        Customer customer = createAndSaveCustomer(tenant);

        ResultActions result = mockMvc.perform(
                get("/api/v1/customers/{id}", customer.getId())
                        .with(user("test-user"))
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId().toString()))
                .andExpect(jsonPath("$.name").value(customer.getName()))
                .andExpect(jsonPath("$.email").value(customer.getEmail()));
    }

    @Test
    void shouldRejectFindByIdWhenUserIsNotAuthenticated() throws Exception {
        Tenant tenant = createAndSaveTenant();
        Customer customer = createAndSaveCustomer(tenant);

        ResultActions result = mockMvc.perform(
                get("/api/v1/customers/{id}", customer.getId())
        );

        result.andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectFindByEmailWhenUserIsNotAuthenticated() throws Exception {
        Tenant tenant = createAndSaveTenant();
        Customer customer = createAndSaveCustomer(tenant);

        ResultActions result = mockMvc.perform(
                get("/api/v1/customers/{id}", customer.getEmail())
        );

        result.andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectCreationWhenEmailAlreadyExists() throws Exception {
        Tenant tenant = createAndSaveTenant();
        Customer existingCustomer = createAndSaveCustomer(tenant);
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Another Customer",
                existingCustomer.getEmail(),
                "85999999998",
                tenant.getId()
        );
        String requestJson = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(
                post("/api/v1/customers")
                        .with(user("test-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
        );

        result.andExpect(status().is4xxClientError());
        assertThat(customerRepository.findAll()).hasSize(1);
    }

    private Tenant createAndSaveTenant() {
        Tenant tenant = Tenant.builder()
                .name("Test Tenant")
                .slug("test-tenant-" + UUID.randomUUID())
                .status(TenantStatus.ACTIVE)
                .build();
        return tenantRepository.saveAndFlush(tenant);
    }

    private Customer createAndSaveCustomer(Tenant tenant) {
        Customer customer = Customer.builder()
                .name("John Test")
                .email("john.test-" + UUID.randomUUID() + "@email.com")
                .phone("859999" + String.format("%05d", (int) (Math.random() * 100000)))
                .tenant(tenant)
                .build();
        return customerRepository.saveAndFlush(customer);
    }
}