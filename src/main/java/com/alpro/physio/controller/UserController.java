package com.alpro.physio.controller;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.alpro.physio.dto.LoginRequest;
import com.alpro.physio.dto.LoginResponse;
import com.alpro.physio.dto.RegisterRequest;
import com.alpro.physio.entity.User;
import com.alpro.physio.security.TokenBlacklist;
import com.alpro.physio.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    private final TokenBlacklist tokenBlacklist;

    public UserController(UserService userService, TokenBlacklist tokenBlacklist) {
        this.userService = userService;
        this.tokenBlacklist = tokenBlacklist;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User createdUser = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully",
                             "userId", createdUser.getUserId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);
        tokenBlacklist.blacklistToken(token);
        return ResponseEntity.ok(Map.of("message", "Successfully logged out"));
    }
}
