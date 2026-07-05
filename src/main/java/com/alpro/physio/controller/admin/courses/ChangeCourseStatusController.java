package com.alpro.physio.controller.admin.courses;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.CourseDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/courses")
public class ChangeCourseStatusController {
    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @PatchMapping("/change-status")
    public ResponseEntity<?> changeCourseStatus(HttpServletRequest request, @RequestParam("courseId") Integer courseId, @RequestParam("status") Integer status) {
        Map<String, Object> response = new LinkedHashMap<>();

        // Validate authentication
        ResponseEntity<?> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        try {
            CourseDTO course = dao.courseDAO().getCourseById(courseId);
            if (course == null) {
                response.put("status", "error");
                response.put("message", "Course not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            course.setStatus(status);
            CourseDTO updatedCourse = dao.courseDAO().changeStatusCourse(course);

            response.put("status", "success");
            response.put("message", "Course status updated successfully.");
            response.put("data", updatedCourse);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error updating course status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
