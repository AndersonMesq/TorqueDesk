package com.andersonmesq.TorqueDesk.customer.service;

import com.andersonmesq.TorqueDesk.customer.dto.CustomerResponse;
import com.andersonmesq.TorqueDesk.customer.exception.CustomerNotFoundException;
import com.andersonmesq.TorqueDesk.customer.mapper.CustomerMapper;
import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    private Customer findCustomer(UUID id){
        return repository.findById(id).orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }

    public CustomerResponse findById(UUID id){
        Customer customer = findCustomer(id);
        return mapper.toResponse(customer);
    }

    public CustomerResponse findByEmail (String email){
        Customer customer = repository.findByEmail(email).orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        return mapper.toResponse(customer);
    }
}