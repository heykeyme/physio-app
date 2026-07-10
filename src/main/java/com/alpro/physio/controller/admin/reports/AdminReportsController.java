package com.alpro.physio.controller.admin.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/reports")
public class AdminReportsController {

    private static final Logger logger = LoggerFactory.getLogger(AdminReportsController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/summary")
    public ResponseEntity<?> getReportsSummary(HttpServletRequest request) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            Map<String, Object> data = new LinkedHashMap<>();

            data.put("enrollmentTotals", dao.reportsDAO().getEnrollmentTotals());
            data.put("enrollmentByCourse", dao.reportsDAO().getEnrollmentByCourse());

            data.put("attendanceTotals", dao.reportsDAO().getAttendanceTotals());
            data.put("attendanceByCourse", dao.reportsDAO().getAttendanceByCourse());

            data.put("revenueTotals", dao.reportsDAO().getRevenueTotals());
            data.put("revenueByCourse", dao.reportsDAO().getRevenueByCourse());

            data.put("feedbackTotals", dao.reportsDAO().getFeedbackTotals());
            data.put("feedbackByCourse", dao.reportsDAO().getFeedbackByCourse());

            response.put("status", "success");
            response.put("data", data);

        } catch (Exception e) {
            logger.error("Failed to retrieve reports summary", e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve reports.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}