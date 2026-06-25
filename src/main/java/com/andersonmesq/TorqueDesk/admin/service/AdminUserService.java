package com.andersonmesq.TorqueDesk.admin.service;

import com.andersonmesq.TorqueDesk.admin.dto.CreateUserRequest;
import com.andersonmesq.TorqueDesk.admin.exception.EmailAlreadyExistsException;
import com.andersonmesq.TorqueDesk.admin.mapper.AdminUserMapper;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyActiveException;
import com.andersonmesq.TorqueDesk.tenant.exception.TenantAlreadyDeactivatedException;
import com.andersonmesq.TorqueDesk.user.dto.UpdateUserRequest;
import com.andersonmesq.TorqueDesk.user.dto.UserResponse;
import com.andersonmesq.TorqueDesk.user.exception.UserNotFoundException;
import com.andersonmesq.TorqueDesk.user.model.User;
import com.andersonmesq.TorqueDesk.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {
    private final UserRepository repository;
    private final AdminUserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    private User findUser(UUID id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public UserResponse findById(UUID id) {
        User user = findUser(id);
        return mapper.toResponse(user);
    }

    public UserResponse findByEmail(String email) {
        User user = repository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
        return mapper.toResponse(user);
    }

    public Page<UserResponse> findAll(@PageableDefault(size = 20) Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional
    public UserResponse create(@Valid CreateUserRequest request){
        if(repository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already exist");
        }
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .build();
        repository.save(user);

        return mapper.toResponse(user);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request){
        User user = findUser(id);
        user.setFullName(request.fullName());
        return mapper.toResponse(user);
    }

    @Transactional
    public void activate(UUID id){
        User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        if(Boolean.TRUE.equals(user.getEnabled())) throw new TenantAlreadyActiveException("Tenant already deactivate");
        user.setEnabled(true);
        repository.save(user);
    }

    @Transactional
    public void deactivate(UUID id){
        User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        if(Boolean.FALSE.equals(user.getEnabled())) throw new TenantAlreadyDeactivatedException("Tenant already deactivate");
        user.setEnabled(false);
        repository.save(user);
    }
}