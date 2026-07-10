package com.alpro.physio.controller.trainer.myclasses;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.UploadPdfDTO;
import com.alpro.physio.dto.VideoDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/trainer/classes/manage")
public class DeleteModuleController {

    private static final Logger logger = LoggerFactory.getLogger(DeleteModuleController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @DeleteMapping("/module")
    public ResponseEntity<?> deleteModule(
            HttpServletRequest request,
            @RequestParam Integer id) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // Manually clean up children first (DB rows + physical files),
            // regardless of whatever ON DELETE behavior the FK actually has —
            // a DB-level CASCADE would remove child rows but never touch files on disk.
            List<VideoDTO> videos = dao.videoDAO().findVideosByModuleId(id);
            List<UploadPdfDTO> pdfs = dao.uploadPdfDAO().findPdfsByModuleId(id);

            int videosDeleted = 0;
            int videoFilesDeleted = 0;

            if (videos != null) {
                for (VideoDTO video : videos) {
                    Integer rows = dao.videoDAO().deleteVideoById(video.getId());
                    if (rows != null && rows > 0) videosDeleted++;

                    try {
                        Path filePath = Paths.get(video.getVideoFilepath());
                        if (Files.deleteIfExists(filePath)) videoFilesDeleted++;
                    } catch (IOException ioe) {
                        logger.warn("Failed to delete video file for video id {}: {}",
                                video.getId(), video.getVideoFilepath(), ioe);
                    }
                }
            }

            int pdfsDeleted = 0;
            int pdfFilesDeleted = 0;

            if (pdfs != null) {
                for (UploadPdfDTO pdf : pdfs) {
                    Integer rows = dao.uploadPdfDAO().deletePdfById(pdf.getId());
                    if (rows != null && rows > 0) pdfsDeleted++;

                    try {
                        Path filePath = Paths.get(pdf.getUploadFilepath());
                        if (Files.deleteIfExists(filePath)) pdfFilesDeleted++;
                    } catch (IOException ioe) {
                        logger.warn("Failed to delete PDF file for pdf id {}: {}",
                                pdf.getId(), pdf.getUploadFilepath(), ioe);
                    }
                }
            }

            // Now safe to delete the module itself — all children are gone
            Integer moduleRowsAffected = dao.moduleDAO().deleteModuleById(id);

            if (moduleRowsAffected == null || moduleRowsAffected == 0) {
                response.put("status", "error");
                response.put("message", "Module not found.");
                return ResponseEntity.status(404).body(response);
            }

            response.put("status", "success");
            response.put("message", "Module and its materials deleted successfully.");
            response.put("videosDeleted", videosDeleted);
            response.put("videoFilesDeleted", videoFilesDeleted);
            response.put("pdfsDeleted", pdfsDeleted);
            response.put("pdfFilesDeleted", pdfFilesDeleted);

        } catch (Exception e) {
            logger.error("Failed to delete module id: {}", id, e);
            response.put("status", "error");
            response.put("message", "Failed to delete module.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}