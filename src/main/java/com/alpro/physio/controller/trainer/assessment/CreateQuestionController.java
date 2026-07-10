package com.alpro.physio.controller.trainer.assessment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alpro.physio.dao.root.Dao;
import com.alpro.physio.dto.CreateQuestionRequest;
import com.alpro.physio.dto.QuestionDTO;
import com.alpro.physio.dto.QuestionOptionDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/trainer/question")
public class CreateQuestionController {

    private static final Logger logger = LoggerFactory.getLogger(CreateQuestionController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<?> createQuestion(
            HttpServletRequest request,
            @RequestBody CreateQuestionRequest body) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // Basic input validation
            if (body.getAssessmentId() == null) {
                response.put("status", "error");
                response.put("message", "assessmentId is required.");
                return ResponseEntity.badRequest().body(response);
            }

            if (body.getQuestionText() == null || body.getQuestionText().isBlank()) {
                response.put("status", "error");
                response.put("message", "questionText is required.");
                return ResponseEntity.badRequest().body(response);
            }

            List<CreateQuestionRequest.OptionRequest> options = body.getOptions();

            if (options == null || options.size() != 4) {
                response.put("status", "error");
                response.put("message", "Exactly 4 options are required.");
                return ResponseEntity.badRequest().body(response);
            }

            long correctCount = options.stream().filter(CreateQuestionRequest.OptionRequest::isCorrect).count();
            if (correctCount != 1) {
                response.put("status", "error");
                response.put("message", "Exactly one option must be marked as correct.");
                return ResponseEntity.badRequest().body(response);
            }

            for (CreateQuestionRequest.OptionRequest opt : options) {
                if (opt.getLabel() == null || opt.getLabel().isBlank()
                        || opt.getText() == null || opt.getText().isBlank()) {
                    response.put("status", "error");
                    response.put("message", "Each option must have a label and text.");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            // Insert the question first, then its 4 options.
            // @Transactional ensures all of this rolls back together if any step fails,
            // so we never end up with a question that has fewer than 4 options.
            QuestionDTO question = dao.questionDAO().insertQuestionByAssessmentId(
                    body.getAssessmentId(), body.getQuestionText());

            List<QuestionOptionDTO> insertedOptions = new ArrayList<>();
            for (CreateQuestionRequest.OptionRequest opt : options) {
                QuestionOptionDTO option = dao.questionOptionDAO().insertOption(
                        question.getId(), opt.getLabel(), opt.getText(), opt.isCorrect());
                insertedOptions.add(option);
            }

            Map<String, Object> questionData = new LinkedHashMap<>();
            questionData.put("questionId", question.getId());
            questionData.put("assessmentId", question.getAssessmentId());
            questionData.put("questionText", question.getQuestionText());
            questionData.put("options", insertedOptions);

            response.put("status", "success");
            response.put("message", "Question created successfully.");
            response.put("data", questionData);

        } catch (Exception e) {
            logger.error("Failed to create question for assessmentId: {}", body.getAssessmentId(), e);
            response.put("status", "error");
            response.put("message", "Failed to create question.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}