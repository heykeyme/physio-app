package com.alpro.physio.controller.admin.manageUsers;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.UserDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/manage-users")
public class AdminSearchUsersController {

    private static final Logger logger = LoggerFactory.getLogger(AdminSearchUsersController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            HttpServletRequest request,
            @RequestParam(required = false) String fullname,
            @RequestParam(defaultValue = "1") int page) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (page < 1) {
                response.put("status", "error");
                response.put("message", "Page number must be 1 or greater.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean hasSearchTerm = fullname != null && !fullname.isBlank();
            List<UserDTO> users;
            int totalUsers;

            if (hasSearchTerm) {
                logger.info("Searching participants/trainers by fullname: '{}', page: {}", fullname, page);
                users = dao.userDAO().searchParticipantsAndTrainersByFullname(fullname, page);
                totalUsers = dao.userDAO().countSearchParticipantsAndTrainersByFullname(fullname);
            } else {
                logger.info("No search term provided, returning participants/trainers list, page: {}", page);
                users = dao.userDAO().findParticipantsAndTrainers(page);
                totalUsers = dao.userDAO().countParticipantsAndTrainers();
            }

            int totalPages = (int) Math.ceil((double) totalUsers / 10);

            List<Map<String, Object>> userList = new ArrayList<>();

            if (users != null && !users.isEmpty()) {
                for (UserDTO user : users) {
                    Map<String, Object> userData = new LinkedHashMap<>();
                    userData.put("id", user.getId());
                    userData.put("userId", user.getUserId());
                    userData.put("email", user.getEmail());
                    userData.put("fullname", user.getFullname());
                    userData.put("status", user.isStatus());
                    String role = dao.masterRoleDAO().findRoleNameById(user.getRoleId());
                    userData.put("role", role);
                    userList.add(userData);
                }
            }

            response.put("status", "success");
            response.put("message", "Users retrieved successfully.");
            response.put("data", userList);
            response.put("currentPage", page);
            response.put("totalPages", totalPages);
            response.put("totalUsers", totalUsers);

        } catch (Exception e) {
            logger.error("Failed to search users", e);
            response.put("status", "error");
            response.put("message", "Failed to search users.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}