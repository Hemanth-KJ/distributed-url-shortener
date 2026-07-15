package com.hemanth.distributedurlshortener.controller;

import com.hemanth.distributedurlshortener.dto.request.RegisterRequest;
import com.hemanth.distributedurlshortener.dto.response.RegisterResponse;
import com.hemanth.distributedurlshortener.response.ApiResponse;
import com.hemanth.distributedurlshortener.service.UserService;
import com.hemanth.distributedurlshortener.dto.request.LoginRequest;
import com.hemanth.distributedurlshortener.dto.response.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        RegisterResponse response = userService.register(request);

        ApiResponse<RegisterResponse> apiResponse =
                ApiResponse.<RegisterResponse>builder()
                        .success(true)
                        .message("User registered successfully")
                        .data(response)
                        .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiResponse);
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = userService.login(request);

        ApiResponse<AuthResponse> apiResponse = ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successful")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}