package com.technokratos.agona.service;

import com.technokratos.agona.dto.RegisterRequest;
import com.technokratos.agona.dto.UpdateProfileRequest;
import com.technokratos.agona.dto.UserProfileForm;
import com.technokratos.agona.enums.Roles;
import com.technokratos.agona.exception.NotFoundException;
import com.technokratos.agona.exception.UserAlreadyExistException;
import com.technokratos.agona.exception.UserNotFoundException;
import com.technokratos.agona.mapper.UserMapper;
import com.technokratos.agona.model.User;
import com.technokratos.agona.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Попытка регистрации с уже существующим именем: {}", request.getUsername());
            throw new UserAlreadyExistException(request.getUsername());
        }
        User user = userMapper.toEntity(request);
        user.setRole(Roles.USER);
        userRepository.save(user);
    }

    public void registerInterviewer(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Попытка регистрации интервьюера с уже существующим именем: {}", request.getUsername());
            throw new UserAlreadyExistException(request.getUsername());
        }
        User user = userMapper.toEntity(request);
        user.setRole(Roles.INTERVIEWER);
        userRepository.save(user);
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public boolean isRegularUser(UUID userId) {
        return getById(userId).getRole() == Roles.USER;
    }

    public UpdateProfileRequest buildProfileForm(UUID userId) {
        User user = getById(userId);
        return UpdateProfileRequest.builder()
                .username(user.getUsername())
                .build();
    }

    @Transactional
    public void updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!user.getUsername().equals(request.getUsername())) {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                log.warn("Попытка смены username на уже существующий: {}", request.getUsername());
                throw new UserAlreadyExistException(request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.save(user);
    }
}
