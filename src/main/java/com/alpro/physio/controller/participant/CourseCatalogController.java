package com.alpro.physio.controller.participant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.CourseDTO;
import com.alpro.physio.dto.UserDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/participant/courses")
public class CourseCatalogController {

    private static final Logger logger = LoggerFactory.getLogger(CourseCatalogController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/catalog")
    public ResponseEntity<?> getUnenrolledCourses(HttpServletRequest request) {

        // Validate authentication
        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            HttpSession session = request.getSession(false);
            String userId = (String) session.getAttribute("userId");
            logger.info("Fetching unenrolled courses for userId: {}", userId);

            List<CourseDTO> courses = dao.enrollCourseDAO().findCoursesNotEnrolledByUser(userId);

            List<Map<String, Object>> courseList = new ArrayList<>();

            if (courses != null && !courses.isEmpty()) {
                for (CourseDTO course : courses) {
                    if (course.getStatus() != null && course.getStatus() == 1) {
                        Map<String, Object> courseData = new LinkedHashMap<>();
                        courseData.put("courseId", course.getId());
                        courseData.put("courseName", course.getCourseName());
                        courseData.put("staffId", course.getStaffId());
                        UserDTO user = dao.userDAO().findByUserId(course.getStaffId());
                        courseData.put("trainerName", user.getFullname());
                        courseData.put("courseDate", course.getCourseDate());
                        courseData.put("courseStartTime", course.getCourseStartTime());
                        courseData.put("courseEndTime", course.getCourseEndTime());
                        courseData.put("coursePrice", course.getCoursePrice());
                        courseList.add(courseData);
                    }
                }
            }

            response.put("status", "success");
            response.put("message", "Available courses retrieved successfully.");
            response.put("data", courseList);

        } catch (Exception e) {
            logger.error("Failed to retrieve unenrolled courses", e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve available courses.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}