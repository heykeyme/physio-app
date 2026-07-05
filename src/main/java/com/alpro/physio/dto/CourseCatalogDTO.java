package com.alpro.physio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class CourseCatalogDTO {

    private Integer id;
    private Integer courseId;
    private String staffId;
    private LocalDate courseDate;
    private LocalTime courseStartTime;
    private LocalTime courseEndTime;
    private BigDecimal coursePrice;

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public LocalDate getCourseDate() {
        return courseDate;
    }

    public void setCourseDate(LocalDate courseDate) {
        this.courseDate = courseDate;
    }

    public LocalTime getCourseStartTime() {
        return courseStartTime;
    }

    public void setCourseStartTime(LocalTime courseStartTime) {
        this.courseStartTime = courseStartTime;
    }

    public LocalTime getCourseEndTime() {
        return courseEndTime;
    }

    public void setCourseEndTime(LocalTime courseEndTime) {
        this.courseEndTime = courseEndTime;
    }

    public BigDecimal getCoursePrice() {
        return coursePrice;
    }

    public void setCoursePrice(BigDecimal coursePrice) {
        this.coursePrice = coursePrice;
    }
}
