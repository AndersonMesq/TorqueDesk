package com.andersonmesq.TorqueDesk.service_order.controller;

import com.andersonmesq.TorqueDesk.service_order.dto.CreateServiceOrderRequest;
import com.andersonmesq.TorqueDesk.service_order.dto.ServiceOrderResponse;
import com.andersonmesq.TorqueDesk.service_order.service.ServiceOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated")
public class ServiceOrderController {
    private final ServiceOrderService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ATTENDANT', 'MECHANIC')")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceOrderResponse create(@RequestBody @Valid CreateServiceOrderRequest request){
        return service.createService(request);
    }
}
