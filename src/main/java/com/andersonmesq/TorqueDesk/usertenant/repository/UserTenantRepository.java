package com.andersonmesq.TorqueDesk.usertenant.repository;

import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTenantRepository extends JpaRepository<UserTenant, UUID> {
        List<UserTenant> findByTenantId(UUID tenantId);
        Optional<UserTenant> findByUserIdAndTenantId(UUID userId, UUID tenantId);
        boolean existsByUserIdAndTenantId(UUID userId, UUID tenantId);
}
