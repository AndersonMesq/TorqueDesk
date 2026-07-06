package com.andersonmesq.TorqueDesk.authentication.service;

import com.andersonmesq.TorqueDesk.security.jwt.JwtService;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import com.andersonmesq.TorqueDesk.usertenant.repository.UserTenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationService{
    private final UserRepository userRepository;
    private final UserTenantRepository userTenantRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    private
}
