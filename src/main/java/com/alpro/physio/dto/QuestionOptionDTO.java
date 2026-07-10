package com.alpro.physio.dto;

public class QuestionOptionDTO {
    private Integer id;
    private Integer questionId;
    private String optionLabel;
    private String optionText;
    
    @com.fasterxml.jackson.annotation.JsonProperty("isCorrect")
    private Boolean isCorrect;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public String getOptionLabel() { return optionLabel; }
    public void setOptionLabel(String optionLabel) { this.optionLabel = optionLabel; }
    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
}