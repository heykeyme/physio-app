package com.alpro.physio.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateQuestionRequest {
    private Integer assessmentId;
    private String questionText;
    private List<OptionRequest> options;

    public Integer getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Integer assessmentId) { this.assessmentId = assessmentId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public List<OptionRequest> getOptions() { return options; }
    public void setOptions(List<OptionRequest> options) { this.options = options; }

    public static class OptionRequest {
        private String label;
        private String text;

        @JsonProperty("isCorrect")
        private boolean isCorrect;

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean isCorrect) { this.isCorrect = isCorrect; }
    }
}