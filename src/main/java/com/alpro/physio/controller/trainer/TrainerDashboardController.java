package com.alpro.physio.controller.trainer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/trainer/dashboard")
public class TrainerDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(TrainerDashboardController.class);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/summary")
    public ResponseEntity<?> getDashboardSummary(HttpServletRequest request) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            HttpSession session = request.getSession(false);
            String trainerId = (String) session.getAttribute("userId");
            logger.info("Fetching dashboard summary for trainerId: {}", trainerId);

            List<CourseDTO> courses = dao.courseDAO().getCoursesByTrainerId(trainerId);

            int activeClassesCount = 0;
            int totalStudentsCount = 0;
            LocalDate today = LocalDate.now();
            List<Map<String, Object>> upcoming = new ArrayList<>();

            if (courses != null) {
                for (CourseDTO course : courses) {
                    if (course.getStatus() != null && course.getStatus() == 1) {
                        activeClassesCount++;
                    }

                    totalStudentsCount += dao.enrollCourseDAO().countEnrollmentByCourseId(course.getId());

                    if (course.getCourseDate() != null && !course.getCourseDate().isBefore(today)) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("courseName", course.getCourseName());
                        item.put("courseDate", course.getCourseDate());
                        item.put("startTime", course.getCourseStartTime() != null
                                ? course.getCourseStartTime().format(TIME_FORMAT) : "");
                        upcoming.add(item);
                    }
                }
            }

            // Sort upcoming classes soonest-first, limit to 5
            upcoming.sort((a, b) -> a.get("courseDate").toString().compareTo(b.get("courseDate").toString()));
            List<Map<String, Object>> upcomingLimited = upcoming.size() > 5 ? upcoming.subList(0, 5) : upcoming;

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("activeClassesCount", activeClassesCount);
            data.put("totalStudentsCount", totalStudentsCount);
            data.put("upcomingClasses", upcomingLimited);

            response.put("status", "success");
            response.put("data", data);

        } catch (Exception e) {
            logger.error("Failed to retrieve trainer dashboard summary", e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve dashboard summary.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}