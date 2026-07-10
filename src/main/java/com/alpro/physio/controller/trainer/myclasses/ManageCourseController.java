package com.alpro.physio.controller.trainer.myclasses;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.ModuleDTO;
import com.alpro.physio.dto.UploadPdfDTO;
import com.alpro.physio.dto.VideoDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/trainer/classes/manage")
public class ManageCourseController {

    private static final Logger logger = LoggerFactory.getLogger(ManageCourseController.class);

    // PLACEHOLDER paths — confirm real storage strategy before using this in production
    private static final String VIDEO_UPLOAD_DIR = "src/main/resources/static/video";
    private static final String PDF_UPLOAD_DIR = "src/main/resources/static/pdf";

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @PostMapping("/module")
    public ResponseEntity<?> createModule(
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
                response.put("message", "Module title is required.");
                return ResponseEntity.badRequest().body(response);
            }

            ModuleDTO module = dao.moduleDAO().insertModuleByCourseId(courseId, title);

            response.put("status", "success");
            response.put("message", "Module created successfully.");
            response.put("data", module);

        } catch (Exception e) {
            logger.error("Failed to create module for courseId: {}", courseId, e);
            response.put("status", "error");
            response.put("message", "Failed to create module.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/video")
    public ResponseEntity<?> uploadVideo(
            HttpServletRequest request,
            @RequestParam Integer moduleId,
            @RequestParam("file") MultipartFile file) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (file == null || file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "No video file provided.");
                return ResponseEntity.badRequest().body(response);
            }

            String originalFilename = file.getOriginalFilename();
            String storedFilename = UUID.randomUUID() + "_" + originalFilename;

            Path uploadPath = Paths.get(VIDEO_UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            Path destination = uploadPath.resolve(storedFilename);
            file.transferTo(destination);

            VideoDTO video = dao.videoDAO().insertVideoByModuleId(
                    moduleId, originalFilename, destination.toString());

            response.put("status", "success");
            response.put("message", "Video uploaded successfully.");
            response.put("data", video);

        } catch (IOException ioe) {
            logger.error("Failed to save video file for moduleId: {}", moduleId, ioe);
            response.put("status", "error");
            response.put("message", "Failed to save video file.");
            response.put("error", ioe.getMessage()); // TEMPORARY - remove before production
        } catch (Exception e) {
            logger.error("Failed to upload video for moduleId: {}", moduleId, e);
            response.put("status", "error");
            response.put("message", "Failed to upload video.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/pdf")
    public ResponseEntity<?> uploadPdf(
            HttpServletRequest request,
            @RequestParam Integer moduleId,
            @RequestParam("file") MultipartFile file) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (file == null || file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "No PDF file provided.");
                return ResponseEntity.badRequest().body(response);
            }

            String originalFilename = file.getOriginalFilename();
            String storedFilename = UUID.randomUUID() + "_" + originalFilename;

            Path uploadPath = Paths.get(PDF_UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            Path destination = uploadPath.resolve(storedFilename);
            file.transferTo(destination);

            UploadPdfDTO pdf = dao.uploadPdfDAO().insertPdfByModuleId(
                    moduleId, originalFilename, destination.toString());

            response.put("status", "success");
            response.put("message", "PDF uploaded successfully.");
            response.put("data", pdf);

        } catch (IOException ioe) {
            logger.error("Failed to save PDF file for moduleId: {}", moduleId, ioe);
            response.put("status", "error");
            response.put("message", "Failed to save PDF file.");
            response.put("error", ioe.getMessage()); // TEMPORARY - remove before production
        } catch (Exception e) {
            logger.error("Failed to upload PDF for moduleId: {}", moduleId, e);
            response.put("status", "error");
            response.put("message", "Failed to upload PDF.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}