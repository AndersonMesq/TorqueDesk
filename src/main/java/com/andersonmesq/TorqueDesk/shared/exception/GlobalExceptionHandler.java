package com.andersonmesq.TorqueDesk.shared.exception;

import com.andersonmesq.TorqueDesk.customer.exception.CustomerAlreadyExistException;
import com.andersonmesq.TorqueDesk.customer.exception.CustomerNotFoundException;
import com.andersonmesq.TorqueDesk.admin.exception.DuplicateSlugException;
import com.andersonmesq.TorqueDesk.admin.exception.UserCreationNotAllowedException;
import com.andersonmesq.TorqueDesk.admin.exception.UserEmailAlreadyExistException;
import com.andersonmesq.TorqueDesk.admin.exception.UserNameAlreadyExistException;
import com.andersonmesq.TorqueDesk.security.exception.InvalidTokenException;
import com.andersonmesq.TorqueDesk.security.exception.UnauthorizedException;
import com.andersonmesq.TorqueDesk.service_order.exception.ServiceOrderAssignmentAlreadyFinishedException;
import com.andersonmesq.TorqueDesk.service_order.exception.ServiceOrderAssignmentAlreadyStartedException;
import com.andersonmesq.TorqueDesk.service_order.exception.ServiceOrderNotFoundException;
import com.andersonmesq.TorqueDesk.tenant.exception.AccessDeniedException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyActiveException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyDeactivatedException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyExistsException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantNotFoundException;
import com.andersonmesq.TorqueDesk.user.exception.InvalidPasswordException;
import com.andersonmesq.TorqueDesk.user.exception.OwnerAlreadyExistException;
import com.andersonmesq.TorqueDesk.user.exception.UserAlreadyActiveException;
import com.andersonmesq.TorqueDesk.user.exception.UserAlreadyDeactivatedException;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.usertenant.exception.UserTenantAlreadyExistException;
import com.andersonmesq.TorqueDesk.usertenant.exception.UserTenantDisabledException;
import com.andersonmesq.TorqueDesk.usertenant.exception.UserTenantNotFoundException;
import com.andersonmesq.TorqueDesk.vehicle.exception.VehicleAlreadyExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerAlreadyExistException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerAlreadyExistException(CustomerAlreadyExistException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomerNotFoundException(CustomerNotFoundException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.andersonmesq.TorqueDesk.admin.exception.UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleAdminUserAlreadyExistsException(
            com.andersonmesq.TorqueDesk.admin.exception.UserAlreadyExistsException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(DuplicateSlugException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateSlugException(DuplicateSlugException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UserCreationNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleUserCreationNotAllowedException(UserCreationNotAllowedException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(UserEmailAlreadyExistException.class)
    public ResponseEntity<ApiErrorResponse> handleUserEmailAlreadyExistException(UserEmailAlreadyExistException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UserNameAlreadyExistException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNameAlreadyExistException(UserNameAlreadyExistException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTokenException(InvalidTokenException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorizedException(UnauthorizedException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ServiceOrderAssignmentAlreadyFinishedException.class)
    public ResponseEntity<ApiErrorResponse> handleServiceOrderAssignmentAlreadyFinishedException(
            ServiceOrderAssignmentAlreadyFinishedException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ServiceOrderAssignmentAlreadyStartedException.class)
    public ResponseEntity<ApiErrorResponse> handleServiceOrderAssignmentAlreadyStartedException(
            ServiceOrderAssignmentAlreadyStartedException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ServiceOrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleServiceOrderNotFoundException(ServiceOrderNotFoundException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(TenantAlreadyActiveException.class)
    public ResponseEntity<ApiErrorResponse> handleTenantAlreadyActiveException(TenantAlreadyActiveException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(TenantAlreadyDeactivatedException.class)
    public ResponseEntity<ApiErrorResponse> handleTenantAlreadyDeactivatedException(
            TenantAlreadyDeactivatedException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(TenantAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleTenantAlreadyExistsException(TenantAlreadyExistsException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTenantNotFoundException(TenantNotFoundException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPasswordException(InvalidPasswordException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(OwnerAlreadyExistException.class)
    public ResponseEntity<ApiErrorResponse> handleOwnerAlreadyExistException(OwnerAlreadyExistException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UserAlreadyActiveException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyActiveException(UserAlreadyActiveException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UserAlreadyDeactivatedException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyDeactivatedException(UserAlreadyDeactivatedException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(com.andersonmesq.TorqueDesk.user.exception.UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExistsException(
            com.andersonmesq.TorqueDesk.user.exception.UserAlreadyExistsException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UserTenantAlreadyExistException.class)
    public ResponseEntity<ApiErrorResponse> handleUserTenantAlreadyExistException(UserTenantAlreadyExistException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UserTenantDisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleUserTenantDisabledException(UserTenantDisabledException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(UserTenantNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserTenantNotFoundException(UserTenantNotFoundException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(VehicleAlreadyExistException.class)
    public ResponseEntity<ApiErrorResponse> handleVehicleAlreadyExistException(VehicleAlreadyExistException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(com.andersonmesq.TorqueDesk.vehicle.exception.VehicleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleVehicleNotFoundException(
            com.andersonmesq.TorqueDesk.vehicle.exception.VehicleNotFoundException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(com.andersonmesq.TorqueDesk.service_order.exception.VehicleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleServiceOrderVehicleNotFoundException(
            com.andersonmesq.TorqueDesk.service_order.exception.VehicleNotFoundException e) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}