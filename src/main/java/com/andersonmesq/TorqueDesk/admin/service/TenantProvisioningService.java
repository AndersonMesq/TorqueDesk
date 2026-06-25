package com.andersonmesq.TorqueDesk.admin.service;

import com.andersonmesq.TorqueDesk.admin.dto.TenantProvisionResponse;
import com.andersonmesq.TorqueDesk.admin.exception.DuplicateSlugException;
import com.andersonmesq.TorqueDesk.shared.util.PasswordGenerator;
import com.andersonmesq.TorqueDesk.shared.util.SlugGenerator;
import com.andersonmesq.TorqueDesk.tenant.dto.CreateTenantRequest;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.usertenant.enums.Role;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TenantProvisioningService {
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final UserTenantRepository userTenantRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantProvisionResponse createTenant(@Valid CreateTenantRequest request) {
        String slug = SlugGenerator.generate(request.companyName());
        if (tenantRepository.existsBySlug(slug)) {
            throw new DuplicateSlugException("Tenant already exists");
        }
        Tenant tenant = Tenant.builder()
                .name(request.companyName())
                .slug(slug)
                .status(TenantStatus.ACTIVE)
                .build();
        tenantRepository.save(tenant);

        String temporaryPassword = PasswordGenerator.generate();
        User owner = User.builder()
                .fullName(request.ownerName())
                .email(request.ownerEmail())
                .password(passwordEncoder.encode(temporaryPassword))
                .enabled(true)
                .build();
        userRepository.save(owner);

        UserTenant userTenant = UserTenant.builder()
                .user(owner)
                .tenant(tenant)
                .role(Role.OWNER)
                .enabled(true)
                .build();
        userTenantRepository.save(userTenant);

        return new TenantProvisionResponse(tenant.getId(), owner.getId(), temporaryPassword);
    }
}