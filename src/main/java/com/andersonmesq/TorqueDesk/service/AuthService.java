package com.andersonmesq.TorqueDesk.service;

import com.andersonmesq.TorqueDesk.model.User;
import com.andersonmesq.TorqueDesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public User register(User user) {

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

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