package com.alpro.physio.controller.admin;

import com.alpro.physio.dao.CourseDAO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.CourseDTO;
import com.alpro.physio.dto.CoursesDTO;
import com.alpro.physio.dto.EnrollCourseDTO;
import com.alpro.physio.dto.UserDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/courses")   
public class CourseController {

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/course-data")
    public ResponseEntity<Map<String, Object>> getCourseData(HttpServletRequest request, @RequestParam Integer courseId) {
        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new HashMap<>();

        CoursesDTO course = dao.coursesDAO().findCourseById(courseId);
        CourseDTO courseCatalog = dao.courseCatalogDAO().findCourseCatalogByCourseId(course.getId());
        UserDTO user = dao.userDAO().findByUserId(courseCatalog.getStaffId());

        Map<String, Object> custMap = new LinkedHashMap<>();
        custMap.put("courseId", course.getId());
        custMap.put("courseName", course.getCourseName());
        custMap.put("assignedTrainer", user.getFullname());
        custMap.put("courseDate", courseCatalog.getCourseDate());
        custMap.put("courseStartTime", courseCatalog.getCourseStartTime());
        custMap.put("courseEndTime", courseCatalog.getCourseEndTime());
        custMap.put("coursePrice", courseCatalog.getCoursePrice());

        response.put("course", custMap);

        return ResponseEntity.ok(response);
    }
}
