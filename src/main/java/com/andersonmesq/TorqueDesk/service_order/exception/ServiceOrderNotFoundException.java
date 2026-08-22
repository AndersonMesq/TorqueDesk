package com.andersonmesq.TorqueDesk.service_order.exception;

public class ServiceOrderNotFoundException extends RuntimeException {
    public ServiceOrderNotFoundException(String message) {
        super(message);
    }
}
