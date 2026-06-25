package com.andersonmesq.TorqueDesk.tenant.exception;

public class TenantNotFoundException extends RuntimeException {
    public TenantNotFoundException(String message) {
        super("Empresa não encontrada" + message);
    }
}
