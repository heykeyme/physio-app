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
import com.alpro.physio.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class LoginController {

    @Autowired
    public Dao dao;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserDTO loginRequest, HttpServletRequest request) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            UserDTO user = dao.userDAO().findByEmail(loginRequest.getEmail());

            if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (!user.isStatus()) {
                response.put("success", false);
                response.put("message", "Account is inactive");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRoleId());

            // Creates a new HTTP session, which sets the JSESSIONID cookie on the response
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("email", user.getEmail());
            session.setAttribute("roleId", user.getRoleId());

            response.put("success", true);
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("userId", user.getUserId());
            response.put("fullname", user.getFullname());
            response.put("role", user.getRoleId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed due to a server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}