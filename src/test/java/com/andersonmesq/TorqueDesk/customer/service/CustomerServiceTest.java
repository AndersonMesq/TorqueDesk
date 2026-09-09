package com.andersonmesq.TorqueDesk.customer.service;

import com.andersonmesq.TorqueDesk.customer.dto.CreateCustomerRequest;
import com.andersonmesq.TorqueDesk.customer.dto.CustomerResponse;
import com.andersonmesq.TorqueDesk.customer.exception.CustomerAlreadyExistException;
import com.andersonmesq.TorqueDesk.customer.exception.CustomerNotFoundException;
import com.andersonmesq.TorqueDesk.customer.mapper.CustomerMapper;
import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldRejectCustomerWhenEmailAlreadyExists() {
        UUID tenantId = UUID.randomUUID();
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John Test",
                "johntest@email.com",
                "85912345678",
                tenantId
        );
        when(customerRepository.existsByEmail(request.email())).thenReturn(true);
        ThrowableAssert.ThrowingCallable action = () -> customerService.create(request);

        assertThatThrownBy(action).isInstanceOf(CustomerAlreadyExistException.class).hasMessage("Customer already exist");
    }

    @Test
    void shouldReturnCustomerWhenRequestIsValid() {
        UUID tenantId = UUID.randomUUID();
        CreateCustomerRequest request = new CreateCustomerRequest(
                "John Test",
                "johntest@email.com",
                "85912345678",
                tenantId
        );
        Tenant tenant = Tenant.builder().id(tenantId).build();
        CustomerResponse expectedResponse = new CustomerResponse(
                null,
                "John Test",
                "johntest@email.com"
        );
        when(customerRepository.existsByEmail(request.email())).thenReturn(false);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(expectedResponse);

        CustomerResponse customerResponse = customerService.create(request);

        assertThat(customerResponse).isEqualTo(expectedResponse);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenIdDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> customerService.findById(customerId);

        assertThatThrownBy(action).isInstanceOf(CustomerNotFoundException.class).hasMessage("Customer not found");
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenEmailDoesNotExist(){
        String email = "emailtest@email.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> customerService.findByEmail(email);

        assertThatThrownBy(action).isInstanceOf(CustomerNotFoundException.class).hasMessage("Customer not found");
    }

    @Test
    void shouldReturnCustomerWhenIdExists() {
        UUID customerId = UUID.randomUUID();
        Customer customer = Customer.builder()
                .id(customerId)
                .name("John Test")
                .email("johntest@email.com")
                .build();
        CustomerResponse expectedResponse = new CustomerResponse(
                customerId,
                "John Test",
                "johntest@email.com"
        );
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(expectedResponse);
        CustomerResponse response = customerService.findById(customerId);

        assertThat(response).isEqualTo(expectedResponse);
    }
}