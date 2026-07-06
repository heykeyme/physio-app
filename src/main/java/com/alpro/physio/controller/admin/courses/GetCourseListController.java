package com.alpro.physio.controller.admin.courses;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.CourseDTO;
import com.alpro.physio.dto.UserDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/courses")
public class GetCourseListController {
    
    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/list")
    public ResponseEntity<?> getCourseList(HttpServletRequest request) {

        // Validate authentication
        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try{
            List<CourseDTO> course = dao.courseDAO().getAllCourses();

            List<Map<String, Object>> courseList = new ArrayList<>();
            for (CourseDTO courseDTO : course) {
                Map<String, Object> courseData = new LinkedHashMap<>();
                courseData.put("courseId", courseDTO.getId());
                courseData.put("courseName", courseDTO.getCourseName());

                UserDTO trainer = dao.userDAO().findByUserId(courseDTO.getStaffId());
                courseData.put("trainerName", trainer.getFullname());

                courseData.put("courseStatus", courseDTO.getStatus());
                courseList.add(courseData);
            }

            response.put("status", "success");
            response.put("message", "Courses retrieved successfully.");
            response.put("data", courseList);
        }catch(Exception e){
            response.put("status", "error");
            response.put("message", "Failed to retrieve courses.");
        }
        return ResponseEntity.ok(response);
    }
}