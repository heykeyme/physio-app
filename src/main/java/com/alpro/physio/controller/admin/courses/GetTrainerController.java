package com.alpro.physio.controller.admin.courses;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.UserDTO;

@RestController
@RequestMapping("/admin/courses")
public class GetTrainerController {
    
    @Autowired
    private Dao dao;

    @GetMapping("/trainer")
    public ResponseEntity<Map<String, Object>> getTrainer() {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            List<UserDTO> trainers = dao.userDAO().findUserByRoleId(4);

            List<Map<String, Object>> trainerList = new ArrayList<>();
            for (UserDTO trainer : trainers) {
                Map<String, Object> trainerData = new LinkedHashMap<>();
                trainerData.put("userId", trainer.getUserId());
                trainerData.put("fullName", trainer.getFullname());
                trainerList.add(trainerData);
            }

            response.put("status", "success");
            response.put("message", "Trainers retrieved successfully.");
            response.put("data", trainerList);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error retrieving trainers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
