package com.andersonmesq.TorqueDesk.customer.exception;

public class customerNotFoundException extends RuntimeException {
    public customerNotFoundException(String message) {
        super(message);
    }
}
