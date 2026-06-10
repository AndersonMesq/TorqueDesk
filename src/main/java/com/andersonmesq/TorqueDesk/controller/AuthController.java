package com.andersonmesq.TorqueDesk.controller;

import com.andersonmesq.TorqueDesk.DTO.RegisterUserDTO;
import com.andersonmesq.TorqueDesk.model.User;
import com.andersonmesq.TorqueDesk.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@RequestBody RegisterUserDTO dto) {
        return authService.register(dto);
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return authService.login(user);
    }
}
