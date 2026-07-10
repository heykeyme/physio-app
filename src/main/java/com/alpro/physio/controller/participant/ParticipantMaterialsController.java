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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.CourseDTO;
import com.alpro.physio.dto.EnrollCourseDTO;
import com.alpro.physio.dto.ModuleDTO;
import com.alpro.physio.dto.UploadPdfDTO;
import com.alpro.physio.dto.VideoDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/participant/materials")
public class ParticipantMaterialsController {

    private static final Logger logger = LoggerFactory.getLogger(ParticipantMaterialsController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/list")
    public ResponseEntity<?> getMyMaterials(
            HttpServletRequest request,
            @RequestParam(required = false) Integer courseId) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            HttpSession session = request.getSession(false);
            String userId = (String) session.getAttribute("userId");
            logger.info("Fetching materials for userId: {}, filtered courseId: {}", userId, courseId);

            List<EnrollCourseDTO> enrollments = dao.enrollCourseDAO().findAllCourseEnrollByUser(userId);
            List<Map<String, Object>> courseList = new ArrayList<>();

            if (enrollments != null && !enrollments.isEmpty()) {
                for (EnrollCourseDTO enrollment : enrollments) {

                    // If a specific courseId was requested, skip every other
                    // enrollment — this is what actually makes each course's
                    // Materials button show only that course's own modules.
                    if (courseId != null && !courseId.equals(enrollment.getCourseId())) {
                        continue;
                    }

                    CourseDTO course = dao.courseDAO().getCourseById(enrollment.getCourseId());

                    if (course == null) {
                        logger.warn("Enrollment {} references missing courseId: {}", enrollment.getId(), enrollment.getCourseId());
                        continue;
                    }

                    List<ModuleDTO> modules = dao.moduleDAO().findModulesByCourseId(course.getId());
                    List<Map<String, Object>> moduleList = new ArrayList<>();

                    if (modules != null) {
                        for (ModuleDTO module : modules) {
                            List<VideoDTO> videos = dao.videoDAO().findVideosByModuleId(module.getId());
                            List<UploadPdfDTO> pdfs = dao.uploadPdfDAO().findPdfsByModuleId(module.getId());

                            List<Map<String, Object>> materialItems = new ArrayList<>();

                            if (videos != null) {
                                for (VideoDTO video : videos) {
                                    Map<String, Object> item = new LinkedHashMap<>();
                                    item.put("type", "video");
                                    item.put("filename", video.getVideoFilename());
                                    item.put("filepath", video.getVideoFilepath());
                                    materialItems.add(item);
                                }
                            }

                            if (pdfs != null) {
                                for (UploadPdfDTO pdf : pdfs) {
                                    Map<String, Object> item = new LinkedHashMap<>();
                                    item.put("type", "pdf");
                                    item.put("filename", pdf.getUploadFilename());
                                    item.put("filepath", pdf.getUploadFilepath());
                                    materialItems.add(item);
                                }
                            }

                            Map<String, Object> moduleData = new LinkedHashMap<>();
                            moduleData.put("moduleId", module.getId());
                            moduleData.put("moduleName", module.getModuleName());
                            moduleData.put("materials", materialItems);
                            moduleList.add(moduleData);
                        }
                    }

                    Map<String, Object> courseData = new LinkedHashMap<>();
                    courseData.put("courseId", course.getId());
                    courseData.put("courseName", course.getCourseName());
                    courseData.put("modules", moduleList);
                    courseList.add(courseData);
                }
            }

            response.put("status", "success");
            response.put("message", "Materials retrieved successfully.");
            response.put("data", courseList);

        } catch (Exception e) {
            logger.error("Failed to retrieve materials", e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve materials.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }

}