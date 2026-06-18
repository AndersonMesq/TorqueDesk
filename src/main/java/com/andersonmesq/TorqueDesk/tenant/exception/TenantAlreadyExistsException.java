package com.andersonmesq.TorqueDesk.tenant.exception;

public class TenantAlreadyExistsException extends RuntimeException {
    public TenantAlreadyExistsException(String message) {
        super("Empresa ja cadastrada " + message);
    }
}
