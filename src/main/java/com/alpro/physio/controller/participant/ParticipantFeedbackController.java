package com.alpro.physio.controller.participant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.CourseDTO;
import com.alpro.physio.dto.EnrollCourseDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/participant/feedback")
public class ParticipantFeedbackController {

    private static final Logger logger = LoggerFactory.getLogger(ParticipantFeedbackController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    /**
     * Returns only completed enrollments that don't already have feedback —
     * this is what populates the "Select Course" dropdown.
     */
    @GetMapping("/eligible-courses")
    public ResponseEntity<?> getEligibleCourses(HttpServletRequest request) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            HttpSession session = request.getSession(false);
            String userId = (String) session.getAttribute("userId");

            List<EnrollCourseDTO> completed = dao.enrollCourseDAO().findCompletedEnrollmentsByUser(userId);
            List<Map<String, Object>> eligibleList = new ArrayList<>();

            if (completed != null) {
                for (EnrollCourseDTO enrollment : completed) {

                    // Skip enrollments that already have feedback submitted
                    if (dao.courseFeedbackDAO().feedbackExistsForEnrollment(enrollment.getId())) {
                        continue;
                    }

                    CourseDTO course = dao.courseDAO().getCourseById(enrollment.getCourseId());
                    if (course == null) continue;

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("enrollmentId", enrollment.getId());
                    item.put("courseName", course.getCourseName());
                    eligibleList.add(item);
                }
            }

            response.put("status", "success");
            response.put("data", eligibleList);

        } catch (Exception e) {
            logger.error("Failed to retrieve eligible courses for feedback", e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve eligible courses.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Submits feedback for a specific completed enrollment.
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitFeedback(HttpServletRequest request, @RequestBody Map<String, Object> body) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            HttpSession session = request.getSession(false);
            String userId = (String) session.getAttribute("userId");

            Integer enrollmentId = (Integer) body.get("enrollmentId");
            Integer rate = (Integer) body.get("rate");
            String review = (String) body.get("review");

            if (enrollmentId == null) {
                response.put("status", "error");
                response.put("message", "enrollmentId is required.");
                return ResponseEntity.badRequest().body(response);
            }

            if (rate == null || rate < 1 || rate > 5) {
                response.put("status", "error");
                response.put("message", "Rating must be between 1 and 5.");
                return ResponseEntity.badRequest().body(response);
            }

            // Ownership check — this enrollment must actually belong to
            // the logged-in user and be marked completed, otherwise a
            // participant could submit feedback for someone else's
            // enrollment just by guessing an enrollmentId.
            EnrollCourseDTO enrollment = dao.enrollCourseDAO().findEnrollCourseById(enrollmentId);

            if (enrollment == null || !enrollment.getUserId().equals(userId)) {
                response.put("status", "error");
                response.put("message", "Enrollment not found.");
                return ResponseEntity.status(403).body(response);
            }

            if (enrollment.getCourseStatus() != 1) {
                response.put("status", "error");
                response.put("message", "This course is not yet completed.");
                return ResponseEntity.status(403).body(response);
            }

            if (dao.courseFeedbackDAO().feedbackExistsForEnrollment(enrollmentId)) {
                response.put("status", "error");
                response.put("message", "Feedback has already been submitted for this course.");
                return ResponseEntity.badRequest().body(response);
            }

            dao.courseFeedbackDAO().insertFeedback(enrollmentId, rate, review);

            response.put("status", "success");
            response.put("message", "Feedback submitted successfully.");

        } catch (Exception e) {
            logger.error("Failed to submit feedback", e);
            response.put("status", "error");
            response.put("message", "Failed to submit feedback.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}