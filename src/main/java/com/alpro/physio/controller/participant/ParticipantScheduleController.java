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
@RequestMapping("/participant/schedule")
public class ParticipantScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(ParticipantScheduleController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/list")
    public ResponseEntity<?> getMySchedule(HttpServletRequest request) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            HttpSession session = request.getSession(false);
            String userId = (String) session.getAttribute("userId");
            logger.info("Fetching schedule for userId: {}", userId);

            List<EnrollCourseDTO> enrollments = dao.enrollCourseDAO().findAllCourseEnrollByUser(userId);
            List<Map<String, Object>> scheduleList = new ArrayList<>();

            if (enrollments != null && !enrollments.isEmpty()) {
                for (EnrollCourseDTO enrollment : enrollments) {
                    CourseDTO course = dao.courseDAO().getCourseById(enrollment.getCourseId());

                    if (course == null) {
                        logger.warn("Enrollment {} references missing courseId: {}", enrollment.getId(), enrollment.getCourseId());
                        continue;
                    }

                    UserDTO trainer = dao.userDAO().findByUserId(course.getStaffId());

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("courseName", course.getCourseName());
                    item.put("courseDate", course.getCourseDate());
                    item.put("courseStartTime", course.getCourseStartTime());
                    item.put("courseEndTime", course.getCourseEndTime());
                    item.put("trainerName", trainer != null ? trainer.getFullname() : "Unknown");
                    scheduleList.add(item);
                }
            }

            // Sort by date, soonest first — matches the trainer weekly schedule convention
            scheduleList.sort((a, b) -> {
                Object dateA = a.get("courseDate");
                Object dateB = b.get("courseDate");
                if (dateA == null || dateB == null) return 0;
                return dateA.toString().compareTo(dateB.toString());
            });

            response.put("status", "success");
            response.put("message", "Schedule retrieved successfully.");
            response.put("data", scheduleList);

        } catch (Exception e) {
            logger.error("Failed to retrieve schedule", e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve schedule.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}