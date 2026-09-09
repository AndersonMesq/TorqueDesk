package com.andersonmesq.TorqueDesk.admin.service;

import com.andersonmesq.TorqueDesk.admin.dto.TenantProvisionResponse;
import com.andersonmesq.TorqueDesk.admin.exception.DuplicateSlugException;
import com.andersonmesq.TorqueDesk.shared.util.SlugGenerator;
import com.andersonmesq.TorqueDesk.tenant.dto.CreateTenantRequest;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import com.andersonmesq.TorqueDesk.user.exception.OwnerAlreadyExistException;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.user.systemrole.SystemRole;
import com.andersonmesq.TorqueDesk.usertenant.model.UserTenant;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import com.andersonmesq.TorqueDesk.usertenant.role.Role;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TenantProvisioningServiceTest {
    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    UserTenantRepository userTenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    TenantProvisioningService tenantProvisioningService;

    @Test
    void shouldProvisionTenantWithOwnerWhenRequestIsValid(){
        CreateTenantRequest request = new CreateTenantRequest(
                "Tenant Test",
                "John test",
                "johntest@email.com",
                "12345678"
        );
        String slug = SlugGenerator.generate(request.companyName());

        when(tenantRepository.existsBySlug(slug)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        TenantProvisionResponse tenantProvisionResponse = tenantProvisioningService.createTenant(request);
        assertThat(tenantProvisionResponse).isNotNull();

        ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(tenantCaptor.capture());
        Tenant savedTenant = tenantCaptor.getValue();
        assertThat(savedTenant.getName()).isEqualTo(request.companyName());
        assertThat(savedTenant.getSlug()).isEqualTo(slug);
        assertThat(savedTenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getFullName()).isEqualTo(request.ownerName());
        assertThat(savedUser.getEmail()).isEqualTo(request.ownerEmail().toLowerCase());
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getSystemRole()).isEqualTo(SystemRole.USER);
        assertThat(savedUser.getEnabled()).isEqualTo(true);

        ArgumentCaptor<UserTenant> userTenantCaptor = ArgumentCaptor.forClass(UserTenant.class);
        verify(userTenantRepository).save(userTenantCaptor.capture());
        UserTenant savedUserTenant = userTenantCaptor.getValue();
        assertThat(savedUserTenant.getUser()).isEqualTo(savedUser);
        assertThat(savedUserTenant.getTenant()).isEqualTo(savedTenant);
        assertThat(savedUserTenant.getRole()).isEqualTo(Role.OWNER);
        assertThat(savedUserTenant.getEnabled()).isEqualTo(true);
    }

    @Test
    void shouldRejectWhenTenantSlugAlreadyExists(){
        CreateTenantRequest request = new CreateTenantRequest(
                "Tenant Test",
                "John test",
                "johntest@email.com",
                "12345678"
        );
        String slug = SlugGenerator.generate(request.companyName());

        when(tenantRepository.existsBySlug(slug)).thenReturn(true);
        ThrowableAssert.ThrowingCallable action = () -> tenantProvisioningService.createTenant(request);

        assertThatThrownBy(action).isInstanceOf(DuplicateSlugException.class).hasMessage("Slug already exists");
    }

    @Test
    void shouldRejectWhenOwnerAlreadyExists(){
        CreateTenantRequest request = new CreateTenantRequest(
                "Tenant Test",
                "John test",
                "johntest@email.com",
                "12345678"
        );

        when(userRepository.existsByEmail(request.ownerEmail().toLowerCase())).thenReturn(true);
        ThrowableAssert.ThrowingCallable action = () -> tenantProvisioningService.createTenant(request);

        assertThatThrownBy(action).isInstanceOf(OwnerAlreadyExistException.class).hasMessage("Owner with this email already exists");
    }
}