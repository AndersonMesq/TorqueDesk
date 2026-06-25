package com.andersonmesq.TorqueDesk.admin.controller;

import com.andersonmesq.TorqueDesk.admin.dto.CreateUserRequest;
import com.andersonmesq.TorqueDesk.admin.service.AdminUserService;
import com.andersonmesq.TorqueDesk.user.dto.UpdateUserRequest;
import com.andersonmesq.TorqueDesk.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminUserController {
    private final AdminUserService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody @Valid CreateUserRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @GetMapping("/email/{email}")
    public UserResponse findByEmail(@RequestParam String email) {
        return service.findByEmail(email);
    }

    @GetMapping
    public Page<UserResponse> findAll(@PageableDefault(size = 20) Pageable pageable) {
        return service.findAll(pageable);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateUserRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@PathVariable UUID id) {
        service.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        service.deactivate(id);
    }
}