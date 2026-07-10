package com.alpro.physio.controller.trainer.myclasses;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
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
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/trainer/classes/manage")
public class DeletePdfController {

    private static final Logger logger = LoggerFactory.getLogger(DeletePdfController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @DeleteMapping("/pdf")
    public ResponseEntity<?> deletePdf(
            HttpServletRequest request,
            @RequestParam Integer id) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // Fetch the record first so we still know the filepath after the DB row is gone
            UploadPdfDTO pdf = dao.uploadPdfDAO().findPdfById(id);

            if (pdf == null) {
                response.put("status", "error");
                response.put("message", "PDF not found.");
                return ResponseEntity.status(404).body(response);
            }

            Integer rowsAffected = dao.uploadPdfDAO().deletePdfById(id);

            if (rowsAffected == null || rowsAffected == 0) {
                response.put("status", "error");
                response.put("message", "PDF not found.");
                return ResponseEntity.status(404).body(response);
            }

            // Attempt to delete the physical file too. If this fails, the DB
            // row is still gone (as requested) — we just report the file
            // cleanup issue separately rather than blocking the whole operation.
            boolean fileDeleted = false;
            try {
                Path filePath = Paths.get(pdf.getUploadFilepath());
                fileDeleted = Files.deleteIfExists(filePath);
            } catch (IOException ioe) {
                logger.warn("DB row for PDF id {} deleted, but failed to delete file at {}",
                        id, pdf.getUploadFilepath(), ioe);
            }

            response.put("status", "success");
            response.put("message", fileDeleted
                    ? "PDF deleted successfully."
                    : "PDF record deleted, but the file could not be removed from disk.");

        } catch (Exception e) {
            logger.error("Failed to delete PDF id: {}", id, e);
            response.put("status", "error");
            response.put("message", "Failed to delete PDF.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}