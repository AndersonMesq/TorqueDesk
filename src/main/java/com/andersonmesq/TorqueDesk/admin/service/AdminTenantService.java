package com.andersonmesq.TorqueDesk.admin.service;

import com.andersonmesq.TorqueDesk.admin.exception.DuplicateSlugException;
import com.andersonmesq.TorqueDesk.shared.util.SlugGenerator;
import com.andersonmesq.TorqueDesk.tenant.dto.TenantResponse;
import com.andersonmesq.TorqueDesk.tenant.dto.UpdateTenantRequest;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyActiveException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyDeactivatedException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantNotFoundException;
import com.andersonmesq.TorqueDesk.tenant.mapper.TenantMapper;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus.INACTIVE;
import static com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus.ACTIVE;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTenantService {
    private final TenantRepository repository;
    private final TenantMapper mapper;

    private Tenant findTenant(UUID id) {
        return repository.findById(id).orElseThrow(() -> new TenantNotFoundException("Tenant not found"));
    }

    public TenantResponse findById(UUID id) {
        Tenant tenant = findTenant(id);
        return mapper.toResponse(tenant);
    }

    public Page<TenantResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional
    public TenantResponse update(UUID id, UpdateTenantRequest request){
        Tenant tenant = findTenant(id);
        tenant.setName(request.companyName());
        String newSlug = SlugGenerator.generate(request.companyName());
        if(repository.existsBySlugAndIdNot(newSlug, id)) {
            throw new DuplicateSlugException("Slug already exists");
        }
        tenant.setSlug(SlugGenerator.generate(request.companyName()));
        return mapper.toResponse(tenant);
    }

    @Transactional
    public void deactivate(UUID id){
        Tenant tenant = findTenant(id);
        if(tenant.getStatus() == INACTIVE) throw new TenantAlreadyDeactivatedException("Tenant already deactivate");
        tenant.setStatus(INACTIVE);
        repository.save(tenant);
    }

    @Transactional
    public void activate(UUID id) {
        Tenant tenant = findTenant(id);
        if(tenant.getStatus() == ACTIVE) throw new TenantAlreadyActiveException("Tenant already active");
        tenant.setStatus(ACTIVE);
        repository.save(tenant);
    }
}