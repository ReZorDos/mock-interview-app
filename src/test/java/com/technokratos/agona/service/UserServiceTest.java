package com.technokratos.agona.service;

import com.technokratos.agona.dto.RegisterRequest;
import com.technokratos.agona.dto.UpdateProfileRequest;
import com.technokratos.agona.enums.Roles;
import com.technokratos.agona.exception.UserAlreadyExistException;
import com.technokratos.agona.exception.UserNotFoundException;
import com.technokratos.agona.mapper.UserMapper;
import com.technokratos.agona.model.User;
import com.technokratos.agona.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void registerUser_newUsername_savesWithUserRole() {
        RegisterRequest req = new RegisterRequest("john", "john@mail.com", "password1");
        User user = new User();
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(userMapper.toEntity(req)).thenReturn(user);

        userService.registerUser(req);

        assertThat(user.getRole()).isEqualTo(Roles.USER);
        verify(userRepository).save(user);
    }

    @Test
    void registerUser_duplicateUsername_throwsUserAlreadyExistException() {
        RegisterRequest req = new RegisterRequest("john", "john@mail.com", "password1");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.registerUser(req))
                .isInstanceOf(UserAlreadyExistException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerInterviewer_newUsername_savesWithInterviewerRole() {
        RegisterRequest req = new RegisterRequest("hr", "hr@mail.com", "password1");
        User user = new User();
        when(userRepository.findByUsername("hr")).thenReturn(Optional.empty());
        when(userMapper.toEntity(req)).thenReturn(user);

        userService.registerInterviewer(req);

        assertThat(user.getRole()).isEqualTo(Roles.INTERVIEWER);
        verify(userRepository).save(user);
    }

    @Test
    void registerInterviewer_duplicateUsername_throwsUserAlreadyExistException() {
        RegisterRequest req = new RegisterRequest("hr", "hr@mail.com", "pass");
        when(userRepository.findByUsername("hr")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.registerInterviewer(req))
                .isInstanceOf(UserAlreadyExistException.class);
    }

    @Test
    void getById_found_returnsUser() {
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThat(userService.getById(userId)).isSameAs(user);
    }

    @Test
    void getById_notFound_throwsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void isRegularUser_roleUser_returnsTrue() {
        User user = new User();
        user.setRole(Roles.USER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThat(userService.isRegularUser(userId)).isTrue();
    }

    @Test
    void isRegularUser_roleInterviewer_returnsFalse() {
        User user = new User();
        user.setRole(Roles.INTERVIEWER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThat(userService.isRegularUser(userId)).isFalse();
    }

    @Test
    void buildProfileForm_returnsUsernameFromUser() {
        User user = new User();
        user.setUsername("alice");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UpdateProfileRequest form = userService.buildProfileForm(userId);

        assertThat(form.getUsername()).isEqualTo("alice");
    }

    @Test
    void updateProfile_sameUsername_noUsernameCheck_updatesPassword() {
        User user = new User();
        user.setUsername("bob");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");

        UpdateProfileRequest req = UpdateProfileRequest.builder()
                .username("bob")
                .newPassword("newpass")
                .build();
        userService.updateProfile(userId, req);

        verify(passwordEncoder).encode("newpass");
        assertThat(user.getPassword()).isEqualTo("encoded");
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_newUsername_notTaken_updatesUsername() {
        User user = new User();
        user.setUsername("bob");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("newname")).thenReturn(Optional.empty());

        UpdateProfileRequest req = UpdateProfileRequest.builder().username("newname").build();
        userService.updateProfile(userId, req);

        assertThat(user.getUsername()).isEqualTo("newname");
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_newUsername_alreadyTaken_throwsException() {
        User user = new User();
        user.setUsername("bob");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("taken")).thenReturn(Optional.of(new User()));

        UpdateProfileRequest req = UpdateProfileRequest.builder().username("taken").build();

        assertThatThrownBy(() -> userService.updateProfile(userId, req))
                .isInstanceOf(UserAlreadyExistException.class);
    }

    @Test
    void updateProfile_blankPassword_doesNotEncodePassword() {
        User user = new User();
        user.setUsername("bob");
        user.setPassword("oldEncoded");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UpdateProfileRequest req = UpdateProfileRequest.builder()
                .username("bob")
                .newPassword("   ")
                .build();
        userService.updateProfile(userId, req);

        verify(passwordEncoder, never()).encode(any());
        assertThat(user.getPassword()).isEqualTo("oldEncoded");
    }

    @Test
    void updateProfile_nullPassword_doesNotEncodePassword() {
        User user = new User();
        user.setUsername("bob");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UpdateProfileRequest req = UpdateProfileRequest.builder().username("bob").build();
        userService.updateProfile(userId, req);

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateProfile_userNotFound_throwsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(userId,
                UpdateProfileRequest.builder().username("x").build()))
                .isInstanceOf(UserNotFoundException.class);
    }
}
