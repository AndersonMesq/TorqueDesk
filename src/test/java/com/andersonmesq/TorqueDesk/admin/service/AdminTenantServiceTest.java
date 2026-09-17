package com.andersonmesq.TorqueDesk.admin.service;

import com.andersonmesq.TorqueDesk.admin.exception.DuplicateSlugException;
import com.andersonmesq.TorqueDesk.shared.util.SlugGenerator;
import com.andersonmesq.TorqueDesk.tenant.dto.TenantResponse;
import com.andersonmesq.TorqueDesk.tenant.dto.UpdateTenantRequest;
import com.andersonmesq.TorqueDesk.tenant.enums.TenantStatus;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyActiveException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyDeactivatedException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantNotFoundException;
import com.andersonmesq.TorqueDesk.tenant.mapper.TenantMapper;
import com.andersonmesq.TorqueDesk.tenant.model.Tenant;
import com.andersonmesq.TorqueDesk.tenant.repository.TenantRepository;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminTenantServiceTest {
    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantMapper tenantMapper;

    @InjectMocks
    private AdminTenantService adminTenantService;

    @Test
    void shouldReturnTenantByIdWhenExists() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder()
                .id(tenantId)
                .name("Tenant Test")
                .status(TenantStatus.ACTIVE)
                .build();
        String slug = SlugGenerator.generate(tenant.getName());
        TenantResponse expectedResponse = new TenantResponse(
                tenantId,
                "Tenant Test",
                slug,
                TenantStatus.ACTIVE
        );
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantMapper.toResponse(tenant)).thenReturn(expectedResponse);

        TenantResponse response = adminTenantService.findById(tenantId);

        assertThat(response).isEqualTo(expectedResponse);
        verify(tenantRepository).findById(tenantId);
        verify(tenantMapper).toResponse(tenant);
    }

    @Test
    void shouldThrowTenantNotFoundExceptionWhenIdNotExists() {
        UUID tenantId = UUID.randomUUID();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable action = () -> adminTenantService.findById(tenantId);

        assertThatThrownBy(action).isInstanceOf(TenantNotFoundException.class).hasMessage("Tenant not found");
        verify(tenantMapper, never()).toResponse(any(Tenant.class));
    }

    @Test
    void shouldUpdateTenantWhenRequestIsValid(){
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder()
                .id(tenantId)
                .name("Tenant Test")
                .slug(SlugGenerator.generate("Tenant Test"))
                .build();
        UpdateTenantRequest request = new UpdateTenantRequest(
                "Tenant Test 2"
        );
        String newSlug = SlugGenerator.generate(request.companyName());
        TenantResponse expectedResponse = new TenantResponse(
                tenantId,
                "Tenant Test 2",
                newSlug,
                TenantStatus.ACTIVE
        );
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.existsBySlugAndIdNot(newSlug, tenantId)).thenReturn(false);
        when(tenantMapper.toResponse(tenant)).thenReturn(expectedResponse);

        TenantResponse response = adminTenantService.update(tenantId, request);

        assertThat(response).isEqualTo(expectedResponse);
        assertThat(tenant.getName()).isEqualTo(request.companyName());
        assertThat(tenant.getSlug()).isEqualTo(newSlug);
        verify(tenantRepository).findById(tenantId);
        verify(tenantRepository).existsBySlugAndIdNot(newSlug, tenantId);
        verify(tenantMapper).toResponse(tenant);
    }

    @Test
    void shouldThrowDuplicateSlugExceptionWhenSlugAlreadyExists() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder()
                .id(tenantId)
                .name("Tenant Test")
                .slug(SlugGenerator.generate("Tenant Test"))
                .build();
        UpdateTenantRequest request = new UpdateTenantRequest(
                "Tenant Test 2"
        );
        String newSlug = SlugGenerator.generate(request.companyName());
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantRepository.existsBySlugAndIdNot(newSlug, tenantId)).thenReturn(true);

        ThrowableAssert.ThrowingCallable action = () -> adminTenantService.update(tenantId, request);

        assertThatThrownBy(action).isInstanceOf(DuplicateSlugException.class).hasMessage("Slug already exists");
        verify(tenantMapper, never()).toResponse(any(Tenant.class));
    }

    @Test
    void shouldDeactivateTenantWhenStatusIsActive(){
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder()
                .id(tenantId)
                .name("Tenant Test")
                .slug(SlugGenerator.generate("Tenant Test"))
                .status(TenantStatus.ACTIVE)
                .build();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        adminTenantService.deactivate(tenantId);

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.INACTIVE);
    }

    @Test
    void shouldRejectDeactivateTenantWhenStatusIsInactive(){
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder()
                .id(tenantId)
                .name("Tenant Test")
                .slug(SlugGenerator.generate("Tenant Test"))
                .status(TenantStatus.INACTIVE)
                .build();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        ThrowableAssert.ThrowingCallable action = () -> adminTenantService.deactivate(tenantId);

        assertThatThrownBy(action).isInstanceOf(TenantAlreadyDeactivatedException.class).hasMessage("Tenant already deactivate");
    }

    @Test
    void shouldActivateTenantWhenStatusIsInactive(){
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder()
                .id(tenantId)
                .name("Tenant Test")
                .slug(SlugGenerator.generate("Tenant Test"))
                .status(TenantStatus.INACTIVE)
                .build();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        adminTenantService.activate(tenantId);

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    void shouldRejectActiveTenantWhenStatusIsActive(){
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder()
                .id(tenantId)
                .name("Tenant Test")
                .slug(SlugGenerator.generate("Tenant Test"))
                .status(TenantStatus.ACTIVE)
                .build();
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        ThrowableAssert.ThrowingCallable action = () -> adminTenantService.activate(tenantId);

        assertThatThrownBy(action).isInstanceOf(TenantAlreadyActiveException.class).hasMessage("Tenant already active");
    }
}