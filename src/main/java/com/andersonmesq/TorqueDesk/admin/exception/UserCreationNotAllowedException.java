package com.andersonmesq.TorqueDesk.admin.exception;

public class UserCreationNotAllowedException extends RuntimeException {
  public UserCreationNotAllowedException(String message) {
    super(message);
  }
}
