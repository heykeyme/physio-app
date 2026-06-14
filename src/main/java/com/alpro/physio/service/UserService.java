package com.alpro.physio.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alpro.physio.dto.LoginRequest;
import com.alpro.physio.dto.LoginResponse;
import com.alpro.physio.dto.RegisterRequest;
import com.alpro.physio.entity.User;
import com.alpro.physio.repository.UserRepository;
import com.alpro.physio.security.JwtUtil;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public User registerUser(RegisterRequest request) {
        // 1. Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        // 2. Build new user
        User user = new User();
        user.setUserId(generateUniqueUserId());
        user.setFullname(request.getFullname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // never store plain text

        // 3. Save
        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        // 1. Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // 2. Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // 3. Generate JWT token using user_id (the UUID we assigned)
        String token = jwtUtil.generateToken(user.getUserId());

        // 4. Return response (never include password)
        return new LoginResponse(
                token,
                user.getUserId(),
                user.getFullname(),
                user.getEmail()
        );
    }

    private String generateUniqueUserId() {
        // UUID collision is astronomically unlikely, but we can add a safeguard
        String uuid;
        do {
            uuid = UUID.randomUUID().toString();
        } while (userRepository.findByUserId(uuid).isPresent()); // avoid collision
        return uuid;
    }

    public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
}