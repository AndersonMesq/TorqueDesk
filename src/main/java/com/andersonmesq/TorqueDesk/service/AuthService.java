package com.andersonmesq.TorqueDesk.service;

import com.andersonmesq.TorqueDesk.DTO.RegisterUserDTO;
import com.andersonmesq.TorqueDesk.enums.Role;
import com.andersonmesq.TorqueDesk.exception.EmailAlreadyExistsException;
import com.andersonmesq.TorqueDesk.model.User;
import com.andersonmesq.TorqueDesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public User register(RegisterUserDTO dto) {
        if (userRepository.existByEmail(dto.email())){
            throw new EmailAlreadyExistsException("E-mail já cadastrado");
        }

        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .password(dto.password())
                .role(Role.ATTENDANT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }

    public String login(User user) {
        User foundUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!foundUser.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        return "Login successful";
    }
}