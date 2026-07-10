package com.alpro.physio.controller.trainer.assessment;

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
import com.alpro.physio.dto.AssessmentDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/trainer/assessment")
public class GetAssessmentDetails {

    private static final Logger logger = LoggerFactory.getLogger(GetAssessmentDetails.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/list")
    public ResponseEntity<?> getAssessmentsByCourse(
            HttpServletRequest request,
            @RequestParam Integer courseId) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            logger.info("Fetching assessments for courseId: {}", courseId);

            List<AssessmentDTO> assessments = dao.assessmentDAO().findAssessmentsByCourseId(courseId);
            List<Map<String, Object>> assessmentList = new ArrayList<>();

            if (assessments != null && !assessments.isEmpty()) {
                for (AssessmentDTO assessment : assessments) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("assessmentId", assessment.getId());
                    item.put("courseId", assessment.getCourseId());
                    item.put("title", assessment.getTitle());
                    assessmentList.add(item);
                }
            }

            response.put("status", "success");
            response.put("message", "Assessments retrieved successfully.");
            response.put("data", assessmentList);

        } catch (Exception e) {
            logger.error("Failed to retrieve assessments for courseId: {}", courseId, e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve assessments.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}