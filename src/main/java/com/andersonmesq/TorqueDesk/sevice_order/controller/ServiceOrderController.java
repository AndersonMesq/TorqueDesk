package com.andersonmesq.TorqueDesk.sevice_order.controller;

import com.andersonmesq.TorqueDesk.sevice_order.dto.CreateServiceOrderRequest;
import com.andersonmesq.TorqueDesk.sevice_order.dto.ServiceOrderResponse;
import com.andersonmesq.TorqueDesk.sevice_order.service.ServiceOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/service")
@RequiredArgsConstructor
public class ServiceOrderController {
    private final ServiceOrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceOrderResponse create(@RequestBody @Valid CreateServiceOrderRequest request){
        return service.createService(request);
    }
}
