package com.alpro.physio.controller.participant;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/participant/certificate")
public class CertificateController {

    private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/data/{courseId}")
    public ResponseEntity<?> getCertificateData(HttpServletRequest request, @PathVariable Integer courseId) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            HttpSession session = request.getSession(false);
            String userId = (String) session.getAttribute("userId");

            List<EnrollCourseDTO> enrollments = dao.enrollCourseDAO().findAllCourseEnrollByUser(userId);

            EnrollCourseDTO matched = null;
            if (enrollments != null) {
                for (EnrollCourseDTO e : enrollments) {
                    if (e.getCourseId() == courseId) {
                        matched = e;
                        break;
                    }
                }
            }

            // Server-side gate — never trust a frontend "Completed" badge alone.
            if (matched == null) {
                response.put("status", "error");
                response.put("message", "You are not enrolled in this course.");
                return ResponseEntity.status(403).body(response);
            }

            if (matched.getCourseStatus() != 1) {
                response.put("status", "error");
                response.put("message", "This course is not yet completed.");
                return ResponseEntity.status(403).body(response);
            }

            CourseDTO course = dao.courseDAO().getCourseById(courseId);
            if (course == null) {
                response.put("status", "error");
                response.put("message", "Course not found.");
                return ResponseEntity.status(404).body(response);
            }

            UserDTO user = dao.userDAO().findByUserId(userId);

            String completionDate = course.getCourseDate() != null
                    ? course.getCourseDate().format(DATE_FORMAT)
                    : "";

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("courseId", courseId);
            data.put("fullName", user.getFullname());
            data.put("courseName", course.getCourseName());
            data.put("completionDate", completionDate);

            response.put("status", "success");
            response.put("data", data);

        } catch (Exception e) {
            logger.error("Failed to retrieve certificate data for courseId: {}", courseId, e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve certificate data.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}