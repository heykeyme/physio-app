package com.alpro.physio.controller.admin.dashboard;

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
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);

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
            int totalUsers = dao.userDAO().countAllUsers();
            int activeCourses = dao.courseDAO().countActiveCourses();

            Map<String, Object> revenueTotals = dao.reportsDAO().getRevenueTotals();
            Object totalRevenue = revenueTotals.get("total_revenue");

            List<Map<String, Object>> recentPayments = dao.paymentTransactionDAO().findRecentSuccessfulPayments(5);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("totalUsers", totalUsers);
            data.put("activeCourses", activeCourses);
            data.put("totalRevenue", totalRevenue);
            data.put("recentActivity", recentPayments);

            response.put("status", "success");
            response.put("data", data);

        } catch (Exception e) {
            logger.error("Failed to retrieve admin dashboard summary", e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve dashboard summary.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}