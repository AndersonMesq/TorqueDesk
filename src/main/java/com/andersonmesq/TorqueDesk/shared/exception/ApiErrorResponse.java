package com.andersonmesq.TorqueDesk.shared.exception;

public record ApiErrorResponse(
        int status,
        String error,
        String message
) {
}
