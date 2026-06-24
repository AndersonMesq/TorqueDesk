package com.andersonmesq.TorqueDesk.sevice_order.exception;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(String message) {
        super(message);
    }
}
