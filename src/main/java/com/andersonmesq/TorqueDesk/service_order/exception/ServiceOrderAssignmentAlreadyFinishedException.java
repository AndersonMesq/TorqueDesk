package com.andersonmesq.TorqueDesk.service_order.exception;

public class ServiceOrderAssignmentAlreadyFinishedException extends RuntimeException {
    public ServiceOrderAssignmentAlreadyFinishedException(String message) {
        super(message);
    }
}
