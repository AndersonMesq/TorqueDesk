package com.andersonmesq.TorqueDesk.user.exception;

public class OwnerAlreadyExistException extends RuntimeException {
    public OwnerAlreadyExistException(String message) {
        super(message);
    }
}
