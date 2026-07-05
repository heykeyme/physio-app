package com.alpro.physio.controller.admin.feedback;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.CourseDTO;
import com.alpro.physio.dto.CourseFeedbackDTO;
import com.alpro.physio.dto.EnrollCourseDTO;
import com.alpro.physio.dto.UserDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/feedback")
public class GetFeedbackController {
    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/list")
    public ResponseEntity<?> getAllFeedback(HttpServletRequest request) {

        // Validate authentication
        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            List<CourseFeedbackDTO> feedback = dao.courseFeedbackDAO().getAllFeedback();

            List<Map<String, Object>> feedbackList = new ArrayList<>();
            for (CourseFeedbackDTO feedbackDTO : feedback) {
                Map<String, Object> feedbackData = new LinkedHashMap<>();

                EnrollCourseDTO enrollCourse = dao.enrollCourseDAO().findEnrollCourseById(feedbackDTO.getEnrollCourseId());
                UserDTO user = dao.userDAO().findByUserId(enrollCourse.getUserId());
                CourseDTO course = dao.courseDAO().getCourseById(enrollCourse.getCourseId());

                feedbackData.put("feedbackId", feedbackDTO.getId());
                feedbackData.put("rate", feedbackDTO.getRate());
                feedbackData.put("review", feedbackDTO.getReview());
                feedbackData.put("feedbackDate", feedbackDTO.getFeedbackDate());
                feedbackData.put("courseName", course.getCourseName());
                feedbackData.put("participantName", user.getFullname());
                

                feedbackList.add(feedbackData);
            }
            response.put("status", "success");
            response.put("data", feedbackList);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error retrieving feedback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
}
