package com.andersonmesq.TorqueDesk.usertenant.service;

import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.usertenant.dto.CreateUserTenantRequest;
import com.andersonmesq.TorqueDesk.usertenant.dto.UserTenantResponse;
import com.andersonmesq.TorqueDesk.usertenant.mapper.UserTenantMapper;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserTenantService {
    private final UserTenantRepository repository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserTenantMapper mapper;

    public UserTenantResponse create(CreateUserTenantRequest request){
        if(repository.existsByUserIdAndTenantId(request.userId(), request.tenantId())){throw new IllegalArgumentException("User already linked to tenant");
        }
        User user = userRepository.findById(request.userId()).orElseThrow(() -> new RuntimeException("User not found"));
        Tenant tenant = tenantRepository.findById(request.tenantId()).orElseThrow(() -> new RuntimeException("Tenant not found"));
        UserTenant userTenant = UserTenant.builder()
                .user(user)
                .tenant(tenant)
                .role(request.role())
                .enabled(true)
                .build();
        repository.save(userTenant);

        return mapper.toResponse(userTenant);
    }
}
