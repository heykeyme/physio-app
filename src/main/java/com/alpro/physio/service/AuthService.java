package com.alpro.physio.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.alpro.physio.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Validates the HTTP Session and the JWT Bearer token.
     * * @param request The HttpServletRequest containing headers and session.
     * @return ResponseEntity with error details if unauthorized, or null if valid.
     */
    public ResponseEntity<Map<String, Object>> validateAuth(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        // 1. Validate Session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.put("status", "error");
            response.put("message", "Unauthorized: No active session found.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 2. Validate Header Existence
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("status", "error");
            response.put("message", "Unauthorized: Missing or invalid Authorization header.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 3. Validate Token Validity
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.put("status", "error");
            response.put("message", "Unauthorized: Invalid or expired JWT token.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Return null indicating authentication succeeded
        return null;
    }
}