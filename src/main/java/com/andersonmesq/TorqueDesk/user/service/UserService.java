package com.andersonmesq.TorqueDesk.user.service;

import com.andersonmesq.TorqueDesk.user.dto.ChangePasswordRequest;
import com.andersonmesq.TorqueDesk.user.dto.UpdateUserRequest;
import com.andersonmesq.TorqueDesk.user.dto.UserResponse;
import com.andersonmesq.TorqueDesk.user.exception.InvalidPasswordException;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.user.mapper.UserMapper;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    private User findUser(UUID id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public UserResponse findMe(UUID userId) {
        User user = findUser(userId);
        return mapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateUserRequest request) {
        User user = findUser(userId);
        user.setFullName(request.fullName());
        return mapper.toResponse(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findUser(userId);
        if(!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password invalid");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }
}