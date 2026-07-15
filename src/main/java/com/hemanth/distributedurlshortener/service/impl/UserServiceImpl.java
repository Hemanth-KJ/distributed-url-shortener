package com.hemanth.distributedurlshortener.service.impl;

import com.hemanth.distributedurlshortener.dto.request.LoginRequest;
import com.hemanth.distributedurlshortener.dto.request.RegisterRequest;
import com.hemanth.distributedurlshortener.dto.response.AuthResponse;
import com.hemanth.distributedurlshortener.dto.response.RegisterResponse;
import com.hemanth.distributedurlshortener.entity.User;
import com.hemanth.distributedurlshortener.exception.EmailAlreadyExistsException;
import com.hemanth.distributedurlshortener.exception.InvalidCredentialsException;
import com.hemanth.distributedurlshortener.repository.UserRepository;
import com.hemanth.distributedurlshortener.service.JwtService;
import com.hemanth.distributedurlshortener.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger =
            LoggerFactory.getLogger(UserServiceImpl.class);

    // Repository for database operations
    private final UserRepository userRepository;

    // Password encoder for encrypting passwords
    private final PasswordEncoder passwordEncoder;

    // JWT service for generating authentication tokens
    private final JwtService jwtService;

    /**
     * Register a new user
     */
    @Override
    public RegisterResponse register(RegisterRequest request) {

        logger.info("Registering user: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {

            logger.warn("Email already exists: {}", request.getEmail());

            throw new EmailAlreadyExistsException("Email already exists");
        }

        // Create User entity
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();

        // Save user to database
        userRepository.save(user);

        logger.info("User registered successfully: {}", user.getEmail());

        // Return response
        return RegisterResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }

    /**
     * Login user
     */
    @Override
    public AuthResponse login(LoginRequest request) {

        logger.info("Login attempt for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            logger.warn("Invalid password for {}", request.getEmail());

            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getEmail());

        logger.info("Login successful for {}", user.getEmail());

        // Return authentication response
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .build();
    }
}