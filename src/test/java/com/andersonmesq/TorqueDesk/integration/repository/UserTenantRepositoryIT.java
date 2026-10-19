package com.andersonmesq.TorqueDesk.integration.repository;

import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import com.andersonmesq.TorqueDesk.usertenant.role.Role;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserTenantRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private UserTenantRepository userTenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistMembership() {
        User user = createUser();
        Tenant tenant = createTenant();
        UserTenant membership = userTenantRepository.save(createMembership(user, tenant));
        entityManager.flush();
        entityManager.clear();

        UserTenant persisted = userTenantRepository.findById(membership.getId()).orElseThrow();

        assertThat(persisted.getUser().getId()).isEqualTo(user.getId());
        assertThat(persisted.getTenant().getId()).isEqualTo(tenant.getId());
        assertThat(persisted.getRole()).isEqualTo(Role.OWNER);
        assertThat(persisted.getEnabled()).isTrue();
    }

    @Test
    void shouldFindMembershipsByTenantId() {
        User user = createUser();
        Tenant tenant = createTenant();
        userTenantRepository.saveAndFlush(createMembership(user, tenant));
        entityManager.clear();

        assertThat(userTenantRepository.findAllByTenantId(tenant.getId())).hasSize(1);
        assertThat(userTenantRepository.findAllByTenantId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void shouldFindMembershipsByUserId() {
        User user = createUser();
        Tenant tenant = createTenant();
        userTenantRepository.saveAndFlush(createMembership(user, tenant));
        entityManager.clear();

        assertThat(userTenantRepository.findAllByUserId(user.getId())).hasSize(1);
        assertThat(userTenantRepository.findAllByUserId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void shouldFindMembershipByUserIdAndTenantId() {
        User user = createUser();
        Tenant tenant = createTenant();
        UserTenant membership = userTenantRepository.saveAndFlush(createMembership(user, tenant));
        entityManager.clear();

        assertThat(userTenantRepository.findByUserIdAndTenantId(user.getId(), tenant.getId()))
                .isPresent().get().satisfies(found -> assertThat(found.getId()).isEqualTo(membership.getId()));
        assertThat(userTenantRepository.findByUserIdAndTenantId(user.getId(), UUID.randomUUID())).isEmpty();
    }

    @Test
    void shouldCheckMembershipExistsByUserIdAndTenantId() {
        User user = createUser();
        Tenant tenant = createTenant();
        userTenantRepository.saveAndFlush(createMembership(user, tenant));
        entityManager.clear();

        assertThat(userTenantRepository.existsByUserIdAndTenantId(user.getId(), tenant.getId())).isTrue();
        assertThat(userTenantRepository.existsByUserIdAndTenantId(user.getId(), UUID.randomUUID())).isFalse();
    }

    private User createUser() {
        return userRepository.saveAndFlush(User.builder()
                .systemRole(SystemRole.USER)
                .fullName("User Tenant Test")
                .userName("user-" + UUID.randomUUID())
                .email("user-tenant-" + UUID.randomUUID() + "@example.com")
                .password("encoded-password")
                .enabled(true)
                .build());
    }

    private Tenant createTenant() {
        return tenantRepository.saveAndFlush(Tenant.builder()
                .name("User Tenant Test")
                .slug("user-tenant-" + UUID.randomUUID())
                .status(TenantStatus.ACTIVE)
                .build());
    }

    private UserTenant createMembership(User user, Tenant tenant) {
        return UserTenant.builder()
                .user(user)
                .tenant(tenant)
                .role(Role.OWNER)
                .enabled(true)
                .build();
    }
}
