package com.andersonmesq.TorqueDesk.user.exception;

public class UserAlreadyDeactivatedException extends RuntimeException {
    public UserAlreadyDeactivatedException(String message) {
        super(message);
    }
}
