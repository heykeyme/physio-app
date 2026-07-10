package com.alpro.physio.controller.trainer.assessment;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.AssessmentDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/trainer/assessment")
public class CreateAssessmentController {

    private static final Logger logger = LoggerFactory.getLogger(CreateAssessmentController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @PostMapping("/create")
    public ResponseEntity<?> createAssessment(
            HttpServletRequest request,
            @RequestParam Integer courseId,
            @RequestParam String title) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (title == null || title.isBlank()) {
                response.put("status", "error");
                response.put("message", "Assessment title is required.");
                return ResponseEntity.badRequest().body(response);
            }

            logger.info("Creating assessment for courseId: {}", courseId);

            AssessmentDTO assessment = dao.assessmentDAO().insertAssessmentByCourseId(courseId, title);

            response.put("status", "success");
            response.put("message", "Assessment created successfully.");
            response.put("data", assessment);

        } catch (Exception e) {
            logger.error("Failed to create assessment for courseId: {}", courseId, e);
            response.put("status", "error");
            response.put("message", "Failed to create assessment.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}