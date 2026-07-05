package com.alpro.physio.controller.admin.courses;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.CourseDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/courses")
public class UpdateCourseController {
    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @PutMapping("/update")
    public ResponseEntity<?> updateCourse(HttpServletRequest request, @RequestBody CourseDTO courseDTO) {

        Map<String, Object> response = new LinkedHashMap<>();

        // Validate authentication
        ResponseEntity<?> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        try {
            CourseDTO updatedCourse = dao.courseDAO().updateCourse(courseDTO);

            response.put("status", "success");
            response.put("message", "Course updated successfully.");
            response.put("data", updatedCourse);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error updating course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
