package com.andersonmesq.TorqueDesk.admin.controller;

import com.andersonmesq.TorqueDesk.admin.dto.TenantProvisionResponse;
import com.andersonmesq.TorqueDesk.admin.service.AdminTenantService;
import com.andersonmesq.TorqueDesk.admin.service.TenantProvisioningService;
import com.andersonmesq.TorqueDesk.tenant.dto.CreateTenantRequest;
import com.andersonmesq.TorqueDesk.tenant.dto.TenantResponse;
import com.andersonmesq.TorqueDesk.tenant.dto.UpdateTenantRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/tenants")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminTenantController {
    private final TenantProvisioningService provisioningService;
    private final AdminTenantService tenantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantProvisionResponse create(@RequestBody @Valid CreateTenantRequest request) {
        return provisioningService.createTenant(request);
    }

    @GetMapping("/{id}")
    public TenantResponse findById(@PathVariable UUID id) {
        return tenantService.findById(id);
    }

    @GetMapping
    public Page<TenantResponse> findAll(@PageableDefault(size = 20) Pageable pageable) {
        return tenantService.findAll(pageable);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        tenantService.deactivate(id);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@PathVariable UUID id) {
        tenantService.activate(id);
    }

    @PutMapping("/{id}")
    public TenantResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateTenantRequest request) {
        return tenantService.update(id, request);
    }
}