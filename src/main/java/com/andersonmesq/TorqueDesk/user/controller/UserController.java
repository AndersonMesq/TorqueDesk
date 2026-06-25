package com.andersonmesq.TorqueDesk.user.controller;

import com.andersonmesq.TorqueDesk.user.dto.ChangePasswordRequest;
import com.andersonmesq.TorqueDesk.user.dto.UpdateUserRequest;
import com.andersonmesq.TorqueDesk.user.dto.UserResponse;
import com.andersonmesq.TorqueDesk.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping("/{id}/me")
    public UserResponse findMe(@PathVariable UUID id) {
        return service.findMe(id);
    }

    @PutMapping("/{id}/profile")
    public UserResponse updateProfile(@PathVariable UUID id, @RequestBody @Valid UpdateUserRequest request) {
        return service.updateProfile(id, request);
    }

    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable UUID id, @RequestBody @Valid ChangePasswordRequest request) {
        service.changePassword(id, request);
    }
}