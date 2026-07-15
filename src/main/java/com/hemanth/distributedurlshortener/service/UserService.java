package com.hemanth.distributedurlshortener.service;

import com.hemanth.distributedurlshortener.dto.request.LoginRequest;
import com.hemanth.distributedurlshortener.dto.request.RegisterRequest;
import com.hemanth.distributedurlshortener.dto.response.AuthResponse;
import com.hemanth.distributedurlshortener.dto.response.RegisterResponse;

public interface UserService {

    RegisterResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}