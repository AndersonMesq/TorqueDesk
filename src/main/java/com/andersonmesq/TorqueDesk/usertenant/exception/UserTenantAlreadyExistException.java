package com.andersonmesq.TorqueDesk.usertenant.exception;

public class UserTenantAlreadyExistException extends RuntimeException {
    public UserTenantAlreadyExistException(String message) {
        super(message);
    }
}
