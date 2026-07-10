package com.alpro.physio.controller.admin.manageUsers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/manage-users")
public class ChangeStatusController {

    private static final Logger logger = LoggerFactory.getLogger(ChangeStatusController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @PatchMapping("/change-status")
    public ResponseEntity<?> changeStatus(HttpServletRequest request, @RequestParam Integer id, @RequestParam Integer status) {

        // Validate authentication
        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // Reject anything other than 1 or 0, since the DAO/DB doesn't enforce this itself
            if (status != 0 && status != 1) {
                response.put("status", "error");
                response.put("message", "Status must be 0 or 1.");
                return ResponseEntity.badRequest().body(response);
            }

            logger.info("Changing status for user id: {} to status: {}", id, status);

            Integer rowsAffected = dao.userDAO().changeStatusUser(id, status);

            if (rowsAffected != null && rowsAffected > 0) {
                response.put("status", "success");
                response.put("message", "User status updated successfully.");
            } else {
                response.put("status", "error");
                response.put("message", "No matching user found.");
                return ResponseEntity.status(404).body(response);
            }

        } catch (Exception e) {
            logger.error("Failed to update user status for id: {}", id, e);
            response.put("status", "error");
            response.put("message", "Failed to update user status.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}