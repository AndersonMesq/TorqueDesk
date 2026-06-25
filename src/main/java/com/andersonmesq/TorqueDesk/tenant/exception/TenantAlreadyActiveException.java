package com.andersonmesq.TorqueDesk.tenant.exception;

public class TenantAlreadyActiveException extends RuntimeException {
    public TenantAlreadyActiveException(String message) {
        super(message);
    }
}
