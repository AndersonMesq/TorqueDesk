package com.andersonmesq.TorqueDesk.shared.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TenantContext {
    public UUID getTenantId() {
        return UUID.fromString(
                "tenant-do-usuario"
        );
    }
//    public UUID getTenantId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if(authentication == null || !authentication.isAuthenticated()) {
//            throw new IllegalStateException("User is not authenticated");
//        }
//        TenantPrincipal principal = (TenantPrincipal) authentication.getPrincipal();
//        return principal.getTenantId();
//    }
}