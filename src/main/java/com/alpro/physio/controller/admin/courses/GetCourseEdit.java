package com.alpro.physio.controller.admin.courses;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.CourseDTO;
import com.alpro.physio.dto.UserDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/admin/courses")
public class GetCourseEdit {
    
    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/edit")
    public ResponseEntity<?> getCourseToEdit(HttpServletRequest request, @RequestParam("courseId") Integer courseId) {

        Map<String, Object> response = new LinkedHashMap<>();

        // Validate authentication
        ResponseEntity<?> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        try {
            CourseDTO course = dao.courseDAO().getCourseById(courseId);
            UserDTO trainer = dao.userDAO().findByUserId(course.getStaffId());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", course.getId());
            data.put("courseName", course.getCourseName());
            data.put("trainerName", trainer != null ? trainer.getFullname() : null);
            data.put("courseDate", course.getCourseDate());
            data.put("courseStartTime", course.getCourseStartTime());
            data.put("courseEndTime", course.getCourseEndTime());
            data.put("coursePrice", course.getCoursePrice());

            response.put("status", "success");
            response.put("data", data);
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to retrieve course details.");
            return ResponseEntity.status(500).body(response);
        }
    }
    
}
