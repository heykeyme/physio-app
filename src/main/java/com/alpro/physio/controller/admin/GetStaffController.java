package com.alpro.physio.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.UserDTO;
import com.alpro.physio.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/admin/courses")   
public class GetStaffController {
    
    @Autowired
    private Dao dao;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/trainers")
    public ResponseEntity<Map<String, Object>> getTrainers(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Validate Session & JWT Token
            HttpSession session = request.getSession(false);
            String authHeader = request.getHeader("Authorization");

            if (session == null || session.getAttribute("userId") == null) {
                response.put("status", "error");
                response.put("message", "Unauthorized: No active session found.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("status", "error");
                response.put("message", "Unauthorized: Missing or invalid Authorization header.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                response.put("status", "error");
                response.put("message", "Unauthorized: Invalid or expired JWT token.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // 2. Fetch trainers from DAO
            List<UserDTO> trainers = dao.userDAO().findTrainer();

            // 3. Filter to only include userId and fullname
            List<Map<String, String>> customData = new java.util.ArrayList<>();
            for (UserDTO trainer : trainers) {
                Map<String, String> trainerMap = new java.util.LinkedHashMap<>();
                trainerMap.put("userId", trainer.getUserId());
                trainerMap.put("fullname", trainer.getFullname());
                customData.add(trainerMap);
            }

            // 4. Build Response
            response.put("status", "success");
            response.put("message", "Trainers fetched successfully");
            response.put("count", customData.size());
            response.put("data", customData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to fetch trainers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
