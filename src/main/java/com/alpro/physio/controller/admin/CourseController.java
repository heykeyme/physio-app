package com.alpro.physio.controller.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.CourseCatalogDTO;
import com.alpro.physio.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/admin/courses")   
public class CourseController {
    
    @Autowired
    private Dao dao;

    @Autowired
    private JwtUtil jwtUtil;

    public static class CourseCreateRequestDTO extends CourseCatalogDTO {
        private String courseName;

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addCourseWithCatalog(@RequestBody CourseCreateRequestDTO payload, HttpServletRequest request) {
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
            if (!jwtUtil.validateToken(token)) { // Adjust method name if your JwtUtil uses a different validation method (e.g., isValidToken)
                response.put("status", "error");
                response.put("message", "Unauthorized: Invalid or expired JWT token.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // 2. Validate mandatory fields
            if (payload.getCourseName() == null || payload.getCourseName().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Course name is required.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            if (payload.getStaffId() == null || payload.getCourseDate() == null || 
                payload.getCourseStartTime() == null || payload.getCourseEndTime() == null) {
                response.put("status", "error");
                response.put("message", "Staff ID, Date, Start Time, and End Time are required.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 3. Insert into `courses` table and retrieve the generated course_id
            Integer generatedCourseId = dao.coursesDAO().addCourse(payload.getCourseName());

            if (generatedCourseId == null || generatedCourseId <= 0) {
                throw new RuntimeException("Failed to generate Course ID.");
            }

            // 4. Insert into `course_catalog` table using the generated course_id
            dao.courseCatalogDAO().addCourseCatalog(
                generatedCourseId,
                payload.getStaffId(),
                payload.getCourseDate(),
                payload.getCourseStartTime(),
                payload.getCourseEndTime(),
                payload.getCoursePrice()
            );

            // 5. Return Success JSON
            response.put("status", "success");
            response.put("message", "Course and Course Catalog added successfully.");
            response.put("courseId", generatedCourseId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            // 6. Return Error JSON if any SQL or Runtime exception occurs
            response.put("status", "error");
            response.put("message", "An error occurred while saving: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
