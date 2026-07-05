package com.alpro.physio.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CourseCatalogDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void addCourseCatalog(Integer courseId, String staffId, LocalDate courseDate, LocalTime courseStartTime, LocalTime courseEndTime, BigDecimal coursePrice) {
        String sql = "INSERT INTO course_catalog " +
                    "(course_id, staff_id, course_date, course_start_time, course_end_time, course_price) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql, courseId, staffId, courseDate, courseStartTime, courseEndTime, coursePrice);
    }
}
