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
import com.alpro.physio.dto.EnrollCourseDTO;
import com.alpro.physio.dto.UserDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/participant/courses")
public class MyEnrollCoursesController {

    private static final Logger logger = LoggerFactory.getLogger(MyEnrollCoursesController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/my-courses")
    public ResponseEntity<?> getMyEnrolledCourses(HttpServletRequest request) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            HttpSession session = request.getSession(false);
            String userId = (String) session.getAttribute("userId");
            logger.info("Fetching enrolled courses for userId: {}", userId);

            List<EnrollCourseDTO> enrollments = dao.enrollCourseDAO().findAllCourseEnrollByUser(userId);
            List<Map<String, Object>> courseList = new ArrayList<>();

            if (enrollments != null && !enrollments.isEmpty()) {
                for (EnrollCourseDTO enrollment : enrollments) {
                    CourseDTO course = dao.courseDAO().getCourseById(enrollment.getCourseId());

                    if (course == null) {
                        // Course was deleted but enrollment row still exists — skip rather than crash
                        logger.warn("Enrollment {} references missing courseId: {}", enrollment.getId(), enrollment.getCourseId());
                        continue;
                    }

                    UserDTO trainer = dao.userDAO().findByUserId(course.getStaffId());

                    Map<String, Object> courseData = new LinkedHashMap<>();
                    courseData.put("enrollmentId", enrollment.getId());
                    courseData.put("courseId", course.getId());
                    courseData.put("courseName", course.getCourseName());
                    courseData.put("trainerName", trainer != null ? trainer.getFullname() : "Unknown");
                    courseData.put("courseDate", course.getCourseDate());
                    courseData.put("courseStartTime", course.getCourseStartTime());
                    courseData.put("courseEndTime", course.getCourseEndTime());
                    courseData.put("courseStatus", enrollment.getCourseStatus());
                    courseData.put("attendanceStatus", enrollment.getAttendanceStatus());
                    courseList.add(courseData);
                }
            }

            response.put("status", "success");
            response.put("message", "Enrolled courses retrieved successfully.");
            response.put("data", courseList);

        } catch (Exception e) {
            logger.error("Failed to retrieve enrolled courses", e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve enrolled courses.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}