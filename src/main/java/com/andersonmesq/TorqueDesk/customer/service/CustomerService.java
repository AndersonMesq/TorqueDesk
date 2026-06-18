package com.andersonmesq.TorqueDesk.customer.service;

import com.andersonmesq.TorqueDesk.customer.dto.CustomerResponse;
import com.andersonmesq.TorqueDesk.customer.exception.customerNotFoundException;
import com.andersonmesq.TorqueDesk.customer.mapper.CustomerMapper;
import com.andersonmesq.TorqueDesk.customer.model.Customer;
import com.andersonmesq.TorqueDesk.customer.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public Customer findCustomer(UUID id){
        return repository.findById(id).orElseThrow(() -> new customerNotFoundException("Customer not found"));
    }

    public CustomerResponse findById(UUID id){
        Customer customer = findCustomer(id);
        return mapper.toResponse(customer);
    }
}
