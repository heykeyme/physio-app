package com.alpro.physio.controller.participant;

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
import com.alpro.physio.dto.EnrollCourseDTO;
import com.alpro.physio.dto.UserDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/participant/dashboard")
public class ParticipantDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(ParticipantDashboardController.class);
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
            String userId = (String) session.getAttribute("userId");
            logger.info("Fetching dashboard summary for userId: {}", userId);

            UserDTO participant = dao.userDAO().findByUserId(userId);

            List<EnrollCourseDTO> allEnrollments = dao.enrollCourseDAO().findAllCourseEnrollByUser(userId);
            List<EnrollCourseDTO> completedEnrollments = dao.enrollCourseDAO().findCompletedEnrollmentsByUser(userId);

            int enrolledCoursesCount = allEnrollments != null ? allEnrollments.size() : 0;
            int certificatesCount = completedEnrollments != null ? completedEnrollments.size() : 0;

            LocalDate today = LocalDate.now();
            int upcomingClassesCount = 0;
            List<Map<String, Object>> todaySchedule = new ArrayList<>();

            if (allEnrollments != null) {
                for (EnrollCourseDTO enrollment : allEnrollments) {
                    CourseDTO course = dao.courseDAO().getCourseById(enrollment.getCourseId());
                    if (course == null || course.getCourseDate() == null) continue;

                    if (!course.getCourseDate().isBefore(today)) {
                        upcomingClassesCount++;
                    }

                    if (course.getCourseDate().isEqual(today)) {
                        UserDTO trainer = dao.userDAO().findByUserId(course.getStaffId());

                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("courseName", course.getCourseName());
                        item.put("startTime", course.getCourseStartTime() != null
                                ? course.getCourseStartTime().format(TIME_FORMAT) : "");
                        item.put("trainerName", trainer != null ? trainer.getFullname() : "Unknown");
                        todaySchedule.add(item);
                    }
                }
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("fullName", participant != null ? participant.getFullname() : "");
            data.put("enrolledCoursesCount", enrolledCoursesCount);
            data.put("upcomingClassesCount", upcomingClassesCount);
            data.put("pendingTasksCount", null); // NOT IMPLEMENTED — no assessment-submission tracking exists yet
            data.put("certificatesCount", certificatesCount);
            data.put("todaySchedule", todaySchedule);

            response.put("status", "success");
            response.put("data", data);

        } catch (Exception e) {
            logger.error("Failed to retrieve dashboard summary", e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve dashboard summary.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}