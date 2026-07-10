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
import com.alpro.physio.dto.ModuleDTO;
import com.alpro.physio.dto.UploadPdfDTO;
import com.alpro.physio.dto.VideoDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/trainer/classes/manage")
public class GetCourseModuleController {

    private static final Logger logger = LoggerFactory.getLogger(GetCourseModuleController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/modules")
    public ResponseEntity<?> getModulesByCourse(
            HttpServletRequest request,
            @RequestParam Integer courseId) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            logger.info("Fetching modules for courseId: {}", courseId);

            List<ModuleDTO> modules = dao.moduleDAO().findModulesByCourseId(courseId);
            List<Map<String, Object>> moduleList = new ArrayList<>();

            if (modules != null && !modules.isEmpty()) {
                for (ModuleDTO module : modules) {
                    List<VideoDTO> videos = dao.videoDAO().findVideosByModuleId(module.getId());
                    List<UploadPdfDTO> pdfs = dao.uploadPdfDAO().findPdfsByModuleId(module.getId());

                    Map<String, Object> moduleData = new LinkedHashMap<>();
                    moduleData.put("moduleId", module.getId());
                    moduleData.put("courseId", module.getCourseId());
                    moduleData.put("moduleName", module.getModuleName());
                    moduleData.put("videos", videos != null ? videos : new ArrayList<>());
                    moduleData.put("pdfs", pdfs != null ? pdfs : new ArrayList<>());

                    moduleList.add(moduleData);
                }
            }

            response.put("status", "success");
            response.put("message", "Modules retrieved successfully.");
            response.put("data", moduleList);

        } catch (Exception e) {
            logger.error("Failed to retrieve modules for courseId: {}", courseId, e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve modules.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}