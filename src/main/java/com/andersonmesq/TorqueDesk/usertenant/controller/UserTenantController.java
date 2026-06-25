package com.andersonmesq.TorqueDesk.usertenant.controller;

import com.andersonmesq.TorqueDesk.usertenant.dto.CreateUserTenantRequest;
import com.andersonmesq.TorqueDesk.usertenant.dto.UserTenantResponse;
import com.andersonmesq.TorqueDesk.usertenant.service.UserTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-tenants")
@RequiredArgsConstructor
public class UserTenantController {
    private final UserTenantService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserTenantResponse create(@RequestBody CreateUserTenantRequest request){
        return service.create(request);
    }
}