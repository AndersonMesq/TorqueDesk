package com.andersonmesq.TorqueDesk.user.service;

import com.andersonmesq.TorqueDesk.security.context.SecurityUtils;
import com.andersonmesq.TorqueDesk.user.dto.ChangePasswordRequest;
import com.andersonmesq.TorqueDesk.user.dto.UpdateUserRequest;
import com.andersonmesq.TorqueDesk.user.dto.UserResponse;
import com.andersonmesq.TorqueDesk.user.exception.InvalidPasswordException;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.user.mapper.UserMapper;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.security.SecurityUtil;
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
    private final SecurityUtils securityUtils;

    private User getCurrentUser() {
        UUID userId = securityUtils.getPrincipal().getUserId();
        return repository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public UserResponse findMe() {
        return mapper.toResponse(getCurrentUser());
    }

    @Transactional
    public UserResponse updateProfile(UpdateUserRequest request) {
        User user = getCurrentUser();
        user.setFullName(request.fullName());
        return mapper.toResponse(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        if(!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password invalid");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }
}