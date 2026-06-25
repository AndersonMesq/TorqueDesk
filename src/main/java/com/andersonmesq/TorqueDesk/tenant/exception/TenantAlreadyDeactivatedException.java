package com.andersonmesq.TorqueDesk.tenant.exception;

public class TenantAlreadyDeactivatedException extends RuntimeException {
    public TenantAlreadyDeactivatedException(String message) {
        super(message);
    }
}
