package com.andersonmesq.TorqueDesk.integration.service;

import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantNotFoundException;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
import com.andersonmesq.TorqueDesk.usertenant.dto.CreateUserTenantRequest;
import com.andersonmesq.TorqueDesk.usertenant.dto.UserTenantResponse;
import com.andersonmesq.TorqueDesk.usertenant.exception.UserTenantAlreadyExistException;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import com.andersonmesq.TorqueDesk.usertenant.role.Role;
import com.andersonmesq.TorqueDesk.usertenant.service.UserTenantService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class UserTenantServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private UserTenantService userTenantService;

    @Autowired
    private UserTenantRepository userTenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @AfterEach
    void cleanUp() {
        userTenantRepository.deleteAll();
        tenantRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateMembershipWithRequestedRole() {
        User user = createUser();
        Tenant tenant = createTenant();

        UserTenantResponse response = userTenantService.create(
                new CreateUserTenantRequest(user.getId(), tenant.getId(), Role.MECHANIC));

        assertThat(response.id()).isNotNull();
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.tenantId()).isEqualTo(tenant.getId());
        assertThat(response.role()).isEqualTo(Role.MECHANIC);
        assertThat(response.enabled()).isTrue();
        assertThat(userTenantRepository.findAllByUserId(user.getId())).hasSize(1);
    }

    @Test
    void shouldCreateOwnerRegardlessOfRequestedRole() {
        User user = createUser();
        Tenant tenant = createTenant();

        UserTenantResponse response = userTenantService.createOwner(
                new CreateUserTenantRequest(user.getId(), tenant.getId(), Role.MECHANIC));

        assertThat(response.role()).isEqualTo(Role.OWNER);
        assertThat(userTenantRepository.findById(response.id()).orElseThrow().getRole()).isEqualTo(Role.OWNER);
    }

    @Test
    void shouldCreateAttendantWithExpectedRole() {
        User attendant = createUser();
        Tenant attendantTenant = createTenant();

        UserTenantResponse attendantResponse = userTenantService.createAttendant(
                new CreateUserTenantRequest(attendant.getId(), attendantTenant.getId(), Role.MECHANIC));

        assertThat(attendantResponse.role()).isEqualTo(Role.ATTENDANT);
    }

    @Test
    void shouldCreateMechanicWithExpectedRole() {
        User mechanic = createUser();
        Tenant mechanicTenant = createTenant();

        UserTenantResponse mechanicResponse = userTenantService.createMechanic(
                new CreateUserTenantRequest(mechanic.getId(), mechanicTenant.getId(), Role.ATTENDANT));

        assertThat(mechanicResponse.role()).isEqualTo(Role.MECHANIC);
    }

    @Test
    void shouldRejectDuplicateMembership() {
        User user = createUser();
        Tenant tenant = createTenant();
        CreateUserTenantRequest request = new CreateUserTenantRequest(user.getId(), tenant.getId(), Role.ATTENDANT);
        userTenantService.create(request);

        assertThatThrownBy(() -> userTenantService.create(request))
                .isInstanceOf(UserTenantAlreadyExistException.class)
                .hasMessage("User already linked to tenant");
        assertThat(userTenantRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldRejectMembershipCreationWhenUserDoesNotExist() {
        Tenant tenant = createTenant();

        assertThatThrownBy(() -> userTenantService.create(
                new CreateUserTenantRequest(UUID.randomUUID(), tenant.getId(), Role.ATTENDANT)))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
        assertThat(userTenantRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectMembershipCreationWhenTenantDoesNotExist() {
        User user = createUser();

        assertThatThrownBy(() -> userTenantService.create(
                new CreateUserTenantRequest(user.getId(), UUID.randomUUID(), Role.ATTENDANT)))
                .isInstanceOf(TenantNotFoundException.class)
                .hasMessage("Tenant not found");
        assertThat(userTenantRepository.findAll()).isEmpty();
    }

    @Test
    void shouldRejectOwnerCreationWhenMembershipAlreadyExists() {
        User user = createUser();
        Tenant tenant = createTenant();
        userTenantService.create(new CreateUserTenantRequest(user.getId(), tenant.getId(), Role.MECHANIC));

        assertThatThrownBy(() -> userTenantService.createOwner(
                new CreateUserTenantRequest(user.getId(), tenant.getId(), Role.MECHANIC)))
                .isInstanceOf(UserTenantAlreadyExistException.class)
                .hasMessage("User already linked to tenant");
    }

    @Test
    void shouldRejectAttendantCreationWhenMembershipAlreadyExists() {
        User user = createUser();
        Tenant tenant = createTenant();
        userTenantService.create(new CreateUserTenantRequest(user.getId(), tenant.getId(), Role.MECHANIC));

        assertThatThrownBy(() -> userTenantService.createAttendant(
                new CreateUserTenantRequest(user.getId(), tenant.getId(), Role.MECHANIC)))
                .isInstanceOf(UserTenantAlreadyExistException.class)
                .hasMessage("User already linked to tenant");
    }

    @Test
    void shouldRejectMechanicCreationWhenMembershipAlreadyExists() {
        User user = createUser();
        Tenant tenant = createTenant();
        userTenantService.create(new CreateUserTenantRequest(user.getId(), tenant.getId(), Role.ATTENDANT));

        assertThatThrownBy(() -> userTenantService.createMechanic(
                new CreateUserTenantRequest(user.getId(), tenant.getId(), Role.ATTENDANT)))
                .isInstanceOf(UserTenantAlreadyExistException.class)
                .hasMessage("User already linked to tenant");
    }

    private User createUser() {
        return userRepository.saveAndFlush(User.builder()
                .systemRole(SystemRole.USER)
                .fullName("Membership User")
                .userName("membership-" + UUID.randomUUID())
                .email("membership-" + UUID.randomUUID() + "@example.com")
                .password("encoded-password")
                .enabled(true)
                .build());
    }

    private Tenant createTenant() {
        return tenantRepository.saveAndFlush(Tenant.builder()
                .name("Membership Tenant")
                .slug("membership-" + UUID.randomUUID())
                .status(TenantStatus.ACTIVE)
                .build());
    }
}
