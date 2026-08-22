package com.andersonmesq.TorqueDesk.vehicle.exception;

public class VehicleAlreadyExistException extends RuntimeException {
    public VehicleAlreadyExistException(String message) {
        super(message);
    }
}
