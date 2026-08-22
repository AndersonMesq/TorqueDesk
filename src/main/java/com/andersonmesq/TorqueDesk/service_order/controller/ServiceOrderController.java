package com.andersonmesq.TorqueDesk.service_order.controller;

import com.andersonmesq.TorqueDesk.service_order.dto.CreateServiceOrderRequest;
import com.andersonmesq.TorqueDesk.service_order.dto.ServiceOrderResponse;
import com.andersonmesq.TorqueDesk.service_order.service.ServiceOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceOrderController {
    private final ServiceOrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceOrderResponse create(@RequestBody @Valid CreateServiceOrderRequest request){
        return service.create(request);
    }

    @PreAuthorize("hasAnyRole('OWNER','MECHANIC')")
    @PostMapping("{id}/assignments/start")
    public ServiceOrderResponse startAssignment(@PathVariable UUID id){
        return service.startAssignment(id);
    }

    @PreAuthorize("hasAnyRole('OWNER','MECHANIC')")
    @PatchMapping("{id}/assignments/finish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finishAssignment(@PathVariable UUID id){
        service.finishAssignment(id);
    }
}
