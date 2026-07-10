package com.alpro.physio.controller.trainer.assessment;

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
import com.alpro.physio.dto.QuestionDTO;
import com.alpro.physio.dto.QuestionOptionDTO;
import com.alpro.physio.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/trainer/question")
public class GetQuestionsController {

    private static final Logger logger = LoggerFactory.getLogger(GetQuestionsController.class);

    @Autowired
    private Dao dao;

    @Autowired
    private AuthService authService;

    @GetMapping("/list")
    public ResponseEntity<?> getQuestions(
            HttpServletRequest request,
            @RequestParam Integer assessmentId) {

        ResponseEntity<Map<String, Object>> authResponse = authService.validateAuth(request);
        if (authResponse != null) {
            return authResponse;
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            List<QuestionDTO> questions = dao.questionDAO().findQuestionsByAssessmentId(assessmentId);
            List<Map<String, Object>> questionList = new ArrayList<>();

            if (questions != null) {
                for (QuestionDTO question : questions) {
                    List<QuestionOptionDTO> options = dao.questionOptionDAO().findOptionsByQuestionId(question.getId());

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("questionId", question.getId());
                    item.put("assessmentId", question.getAssessmentId());
                    item.put("questionText", question.getQuestionText());
                    item.put("options", options != null ? options : new ArrayList<>());
                    questionList.add(item);
                }
            }

            response.put("status", "success");
            response.put("message", "Questions retrieved successfully.");
            response.put("data", questionList);

        } catch (Exception e) {
            logger.error("Failed to retrieve questions for assessmentId: {}", assessmentId, e);
            response.put("status", "error");
            response.put("message", "Failed to retrieve questions.");
            response.put("error", e.getMessage()); // TEMPORARY - remove before production
        }

        return ResponseEntity.ok(response);
    }
}