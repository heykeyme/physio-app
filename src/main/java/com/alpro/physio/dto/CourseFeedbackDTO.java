package com.alpro.physio.dto;

public class CourseFeedbackDTO {

    private Integer id;
    private Integer enrollCourseId;
    private Integer rate;
    private String review;

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEnrollCourseId() {
        return enrollCourseId;
    }

    public void setEnrollCourseId(Integer enrollCourseId) {
        this.enrollCourseId = enrollCourseId;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
