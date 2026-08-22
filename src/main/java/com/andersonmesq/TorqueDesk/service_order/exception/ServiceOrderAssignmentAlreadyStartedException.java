package com.andersonmesq.TorqueDesk.service_order.exception;

public class ServiceOrderAssignmentAlreadyStartedException extends RuntimeException {
    public ServiceOrderAssignmentAlreadyStartedException(String message) {
        super(message);
    }
}
