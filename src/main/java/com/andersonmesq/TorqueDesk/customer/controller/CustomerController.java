package com.andersonmesq.TorqueDesk.customer.controller;

import com.andersonmesq.TorqueDesk.customer.dto.CustomerResponse;
import com.andersonmesq.TorqueDesk.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService service;

    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable UUID id){
        return service.findById(id);
    }
}
