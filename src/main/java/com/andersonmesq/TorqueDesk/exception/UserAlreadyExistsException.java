package com.andersonmesq.TorqueDesk.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("Email ja cadastrado: " + email);
    }
}