package com.alpro.physio.controller.trainer.schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.CourseDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/trainer/schedule")
public class GetWeeklyScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(GetWeeklyScheduleController.class);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklySchedule(
            HttpServletRequest request,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            HttpSession session = request.getSession(false);
            String staffId = (String) session.getAttribute("userId");

            // Default to the current week (Monday–Sunday) if no range is given
            if (startDate == null || endDate == null) {
                LocalDate today = LocalDate.now();
                startDate = today.with(DayOfWeek.MONDAY);
                endDate = today.with(DayOfWeek.SUNDAY);
            }

            if (startDate.isAfter(endDate)) {
                response.put("status", "error");
                response.put("message", "startDate must not be after endDate.");
                return ResponseEntity.badRequest().body(response);
            }

            logger.info("Fetching weekly schedule for staffId: {}, {} to {}", staffId, startDate, endDate);

            List<CourseDTO> courses = dao.courseDAO().findWeeklyScheduleByStaffId(staffId, startDate, endDate);
            List<Map<String, Object>> scheduleList = new ArrayList<>();

            if (courses != null && !courses.isEmpty()) {
                for (CourseDTO course : courses) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("courseName", course.getCourseName());
                    item.put("day", course.getCourseDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
                    item.put("date", course.getCourseDate());
                    item.put("time", course.getCourseStartTime().format(TIME_FORMAT)
                            + " - " + course.getCourseEndTime().format(TIME_FORMAT));
                    scheduleList.add(item);
                }
            }

            response.put("status", "success");
            response.put("message", "Weekly schedule retrieved successfully.");
            response.put("data", scheduleList);
            response.put("startDate", startDate);
            response.put("endDate", endDate);

        } catch (Exception e) {
            logger.error("Failed to retrieve weekly schedule", e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve weekly schedule.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}