package com.alpro.physio.controller.trainer.myclasses;

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
import com.alpro.physio.dto.EnrollCourseDTO;
import com.alpro.physio.dto.UserDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/trainer/classes")
public class ManageAttendeesController {

    private static final Logger logger = LoggerFactory.getLogger(ManageAttendeesController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/attendance")
    public ResponseEntity<?> manageAttendees(HttpServletRequest request,
                                              @RequestParam Integer courseId) {

        // Validate authentication
        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            logger.info("Fetching attendees for courseId: {}", courseId);

            List<EnrollCourseDTO> participants = dao.enrollCourseDAO().findAllParticipantByCourseId(courseId);
            logger.info("Participants found: {}", (participants != null ? participants.size() : "null"));

            List<Map<String, Object>> attendeeList = new ArrayList<>();

            if (participants != null && !participants.isEmpty()) {
                for (EnrollCourseDTO participant : participants) {
                    Map<String, Object> attendeeData = new LinkedHashMap<>();
                    attendeeData.put("courseId", participant.getCourseId());
                    attendeeData.put("userId", participant.getUserId());
                    attendeeData.put("attendanceStatus", participant.getAttendanceStatus());

                    UserDTO user = dao.userDAO().findByUserId(participant.getUserId());
                    attendeeData.put("participantName", user.getFullname());

                    attendeeList.add(attendeeData);
                }
            }

            response.put("status", "success");
            response.put("message", "Attendees retrieved successfully.");
            response.put("data", attendeeList);
        } catch (Exception e) {
            logger.error("Failed to retrieve attendees", e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve attendees.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}