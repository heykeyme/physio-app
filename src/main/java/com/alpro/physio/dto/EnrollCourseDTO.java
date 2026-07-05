package com.alpro.physio.dto;

public class EnrollCourseDTO {

    private int id;
    private String userId;
    private int courseId;
    private byte courseStatus;

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public byte getCourseStatus() {
        return courseStatus;
    }

    public void setCourseStatus(byte courseStatus) {
        this.courseStatus = courseStatus;
    }
}