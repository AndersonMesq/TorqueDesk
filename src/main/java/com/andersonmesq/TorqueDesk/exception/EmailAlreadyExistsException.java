package com.andersonmesq.TorqueDesk.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Email ja cadastrado: " + email);
    }
}
