package com.college.expensetracker.service;

import com.college.expensetracker.dto.UserRegistrationDto;
import com.college.expensetracker.model.User;
import com.college.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryService categoryService;

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username '" + dto.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already registered.");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        User user = User.builder()
            .username(dto.getUsername().toLowerCase())
            .password(passwordEncoder.encode(dto.getPassword()))
            .email(dto.getEmail().toLowerCase())
            .fullName(dto.getFullName())
            .phone(dto.getPhone())
            .build();

        user = userRepository.save(user);
        categoryService.seedDefaultCategoriesForUser(user);
        return user;
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new com.college.expensetracker.exception.ResourceNotFoundException("User", 0L));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User updateProfile(User user, String fullName, String email, String phone) {
        if (!user.getEmail().equals(email.toLowerCase()) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already in use.");
        }
        user.setFullName(fullName);
        user.setEmail(email.toLowerCase());
        user.setPhone(phone);
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword, PasswordEncoder encoder) {
        if (!encoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }
}
