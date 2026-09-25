package com.andersonmesq.TorqueDesk.integration.repository;

import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CustomerRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistCustomer(){
        Tenant tenant = createTenant();
        Customer customer = createCustomer(tenant);

        Customer savedCustomer = customerRepository.save(customer);
        entityManager.flush();
        entityManager.clear();

        Customer persistedCustomer = customerRepository.findById(savedCustomer.getId()).orElseThrow();
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(persistedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(persistedCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(persistedCustomer.getPhone()).isEqualTo(customer.getPhone());
        assertThat(persistedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(persistedCustomer.getTenant().getId()).isEqualTo(tenant.getId());
    }

    @Test
    void shouldFindCustomerById(){
        Tenant tenant = createTenant();
        Customer customer = createCustomer(tenant);
        Customer savedCustomer = customerRepository.save(customer);
        entityManager.flush();
        entityManager.clear();

        Customer foundCustomer = customerRepository.findById(savedCustomer.getId()).orElseThrow();

        assertThat(foundCustomer.getId()).isEqualTo(savedCustomer.getId());
        assertThat(foundCustomer.getEmail()).isEqualTo(customer.getEmail());
    }

    @Test
    void shouldReturnEmptyWhenCustomerDoesNotExist(){
        UUID nonExistentId = UUID.randomUUID();

        var result = customerRepository.findById(nonExistentId);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindCustomerByEmail(){
        Tenant tenant = createTenant();
        Customer customer = createCustomer(tenant);
        customerRepository.save(customer);
        entityManager.flush();
        entityManager.clear();

        var result = customerRepository.findByEmail(customer.getEmail());

        assertThat(result).isPresent().get().extracting(Customer::getEmail).isEqualTo(customer.getEmail());
    }

    @Test
    void shouldReturnEmptyWhenCustomerEmailDoesNotExist(){
        String nonExistentEmail = "nonexistent@email.com";

        var result = customerRepository.findByEmail(nonExistentEmail);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenCustomerEmailExist(){
        Tenant tenant = createTenant();
        Customer customer = createCustomer(tenant);
        customerRepository.save(customer);
        entityManager.flush();
        entityManager.clear();

        boolean exists = customerRepository.existsByEmail(customer.getEmail());

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWheCustomerEmailDoesNotExist(){
        String nonExistentEmail = "nonexistent@email.com";

        boolean exists = customerRepository.existsByEmail(nonExistentEmail);

        assertThat(exists).isFalse();
    }

    @Test
    void shouldPersistCustomerWithCorrectTenantRelationship(){
        Tenant tenant = createTenant();
        Customer customer = createCustomer(tenant);

        Customer savedCustomer = customerRepository.save(customer);
        entityManager.flush();
        entityManager.clear();

        Customer persistedCustomer = customerRepository.findById(savedCustomer.getId()).orElseThrow();
        assertThat(persistedCustomer.getTenant()).isNotNull();
        assertThat(persistedCustomer.getTenant().getId()).isEqualTo(tenant.getId());
        assertThat(persistedCustomer.getTenant().getName()).isEqualTo(tenant.getName());
        assertThat(persistedCustomer.getTenant().getSlug()).isEqualTo(tenant.getSlug());
        assertThat(persistedCustomer.getTenant().getStatus()).isEqualTo(tenant.getStatus());
    }

    @Test
    void shouldPersistMultipleCustomersForSameTenant(){
        Tenant tenant = createTenant();
        Customer customer1 = createCustomer(
                tenant,
                "Customer one",
                "customer.one@email.com",
                "85999990001"
        );
        Customer customer2 = createCustomer(
                tenant,
                "Customer two",
                "customer.two@email.com",
                "85999990002"
        );
        Customer savedCustomer1 = customerRepository.save(customer1);
        Customer savedCustomer2 = customerRepository.save(customer2);
        entityManager.flush();
        entityManager.clear();

        Customer persistedCustomer1 = customerRepository.findById(savedCustomer1.getId()).orElseThrow();
        Customer persistedCustomer2 = customerRepository.findById(savedCustomer2.getId()).orElseThrow();
        assertThat(persistedCustomer1.getTenant().getId()).isEqualTo(tenant.getId());
        assertThat(persistedCustomer2.getTenant().getId()).isEqualTo(tenant.getId());
        assertThat(persistedCustomer1.getId()).isNotEqualTo(persistedCustomer2.getId());
    }

    @Test
    void shouldRejectCustomerWithDuplicateEmail(){
        Tenant tenant = createTenant();
        Customer firstCustomer = createCustomer(
                tenant,
                "First Customer",
                "duplicate@email.com",
                "85999990001"
        );
        Customer secondCustomer = createCustomer(
                tenant,
                "second Customer",
                "duplicate@email.com",
                "85999990002"
        );
        customerRepository.saveAndFlush(firstCustomer);

        assertThatThrownBy(() -> customerRepository.saveAndFlush(secondCustomer)).isInstanceOf(DataIntegrityViolationException.class);
    }

    private Tenant createTenant(){
        Tenant tenant = Tenant.builder()
                .name("TorqueDesk")
                .slug("torquedesk")
                .status(TenantStatus.ACTIVE)
                .build();
        return  tenantRepository.save(tenant);
    }

    private Customer createCustomer(Tenant tenant) {
        return createCustomer(
                tenant,
                "John Test",
                "john.test@email.com",
                "85999990000"
        );
    }

    private Customer createCustomer(
            Tenant tenant,
            String name,
            String email,
            String phone
    ) {
        return Customer.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .tenant(tenant)
                .build();
    }
}