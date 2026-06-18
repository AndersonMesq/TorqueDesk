package com.andersonmesq.TorqueDesk.tenant.repository;

import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    boolean existsBySlugAndIdNot(String slug, UUID id);
    boolean existsBySlug(String slug);
    Optional<Tenant> findBySlug(String slug);
    boolean existsByName(String name);
}
