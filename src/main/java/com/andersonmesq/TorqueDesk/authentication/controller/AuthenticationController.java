package com.andersonmesq.TorqueDesk.authentication.controller;

import com.andersonmesq.TorqueDesk.authentication.dto.request.LoginRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.request.RefreshTokenRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.request.SelectTenantRequest;
import com.andersonmesq.TorqueDesk.authentication.dto.response.AuthenticationResponse;
import com.andersonmesq.TorqueDesk.authentication.dto.response.LoginResponse;
import com.andersonmesq.TorqueDesk.authentication.dto.response.RefreshTokenResponse;
import com.andersonmesq.TorqueDesk.authentication.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService service;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request){
        return service.login(request);
    }

    @PostMapping("/tenant")
    public AuthenticationResponse selectTenant(@RequestBody @Valid SelectTenantRequest request){
        return service.selectTenant(request);
    }

    @PostMapping("/refresh")
    public RefreshTokenResponse refresh(@RequestBody @Valid RefreshTokenRequest request){
        return service.refreshToken(request);
    }
}