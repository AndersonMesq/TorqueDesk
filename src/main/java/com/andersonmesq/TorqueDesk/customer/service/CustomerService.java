package com.andersonmesq.TorqueDesk.customer.service;

import com.andersonmesq.TorqueDesk.customer.dto.CreateCustomerRequest;
import com.andersonmesq.TorqueDesk.customer.dto.CustomerResponse;
import com.andersonmesq.TorqueDesk.customer.exception.CustomerAlreadyExistException;
import com.andersonmesq.TorqueDesk.customer.exception.CustomerNotFoundException;
import com.andersonmesq.TorqueDesk.customer.mapper.CustomerMapper;
import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantNotFoundException;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {
    private final CustomerRepository repository;
    private final TenantRepository tenantRepository;
    private final CustomerMapper mapper;

    private Customer findCustomer(UUID id) {
        return repository.findById(id).orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }

    @Transactional
    public CustomerResponse create(@Valid CreateCustomerRequest request) {
        if (repository.existsByEmail(request.email())) throw new CustomerAlreadyExistException("Customer already exist");
        Tenant tenant = tenantRepository.findById(request.tenantId()).orElseThrow(() -> new TenantNotFoundException("Tenant not found"));
        Customer customer = Customer.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .tenant(tenant)
                .build();
        repository.save(customer);
        return mapper.toResponse(customer);
    }

    public CustomerResponse findById(UUID id) {
        Customer customer = findCustomer(id);
        return mapper.toResponse(customer);
    }

    public CustomerResponse findByEmail(String email) {
        Customer customer = repository.findByEmail(email).orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        return mapper.toResponse(customer);
    }
}