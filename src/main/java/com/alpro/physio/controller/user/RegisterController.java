package com.alpro.physio.controller.user;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.UserDTO;

@RestController
public class RegisterController {
    
    @Autowired
    public Dao dao;

    private final BCryptPasswordEncoder  passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody UserDTO userDTO) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (dao.userDAO().isEmailExists(userDTO.getEmail())) {
                response.put("success", false);
                response.put("message", "Email already registered");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            String hashedPassword = passwordEncoder.encode(userDTO.getPassword());

            dao.userDAO().registerUser(userDTO.getEmail(), userDTO.getFullname(), hashedPassword);

            response.put("success", true);
            response.put("message", "User registered successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed due to a server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
