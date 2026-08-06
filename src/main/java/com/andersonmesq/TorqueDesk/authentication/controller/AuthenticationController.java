package com.andersonmesq.TorqueDesk.authentication.controller;

import com.andersonmesq.TorqueDesk.authentication.dto.request.LoginRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.request.RefreshRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.request.RefreshTokenRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.request.WorkspaceSelectionRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.response.*;
import com.andersonmesq.TorqueDesk.authentication.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AuthenticationController {
    private final AuthenticationService service;

    @PostMapping("/login")
    @PreAuthorize("IsAnonymous()")
    public LoginResponse login(@RequestBody @Valid LoginRequest request){
        return service.login(request);
    }

    @PostMapping("/tenant")
    public WorkspaceSelectionResponse selectWorkspace(@RequestBody @Valid WorkspaceSelectionRequest request){
        return service.selectWorkspace(request);
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@RequestBody @Valid RefreshRequest request) {
        return service.refresh(request);
    }
}