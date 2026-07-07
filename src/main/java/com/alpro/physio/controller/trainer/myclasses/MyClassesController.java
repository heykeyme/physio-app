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
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.CourseDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/trainer/classes")
public class MyClassesController {

    private static final Logger logger = LoggerFactory.getLogger(MyClassesController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/list")
    public ResponseEntity<?> getClassList(HttpServletRequest request) {

        // Validate authentication
        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // Get logged-in trainer's userId from session
            HttpSession session = request.getSession(false);
            String trainerId = (String) session.getAttribute("userId");
            logger.info("Fetching classes for trainerId: {}", trainerId);

            List<CourseDTO> courses = dao.courseDAO().getCoursesByTrainerId(trainerId);
            logger.info("Courses found: {}", (courses != null ? courses.size() : "null"));

            List<Map<String, Object>> classList = new ArrayList<>();

            if (courses != null && !courses.isEmpty()) {
                for (CourseDTO courseDTO : courses) {
                    Map<String, Object> classData = new LinkedHashMap<>();
                    classData.put("courseId", courseDTO.getId());
                    classData.put("courseName", courseDTO.getCourseName());
                    classData.put("courseStatus", courseDTO.getStatus());

                    Integer moduleCount = dao.moduleDAO().findTotalModuleByCourseId(courseDTO.getId());
                    classData.put("totalModule", moduleCount);

                    int enrollmentCount = dao.enrollCourseDAO().countEnrollmentByCourseId(courseDTO.getId());
                    classData.put("totalParticipant", enrollmentCount);
                    classList.add(classData);
                }
            }

            response.put("status", "success");
            response.put("message", "Classes retrieved successfully.");
            response.put("data", classList);
        } catch (Exception e) {
            logger.error("Failed to retrieve classes", e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve classes.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }
        return ResponseEntity.ok(response);
    }
}