package com.alpro.physio.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.alpro.physio.dto.CourseDTO;

@Repository
public class CourseDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void addCourseCatalog(Integer courseId, String staffId, LocalDate courseDate, LocalTime courseStartTime, LocalTime courseEndTime, BigDecimal coursePrice) {
        String sql = "INSERT INTO course_catalog " +
                    "(course_id, staff_id, course_date, course_start_time, course_end_time, course_price) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql, courseId, staffId, courseDate, courseStartTime, courseEndTime, coursePrice);
    }

    public List<CourseDTO> findAllCourses() {
		String sql = "SELECT * FROM course_catalog";
		return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CourseDTO.class));
	}

    public CourseDTO findCourseCatalogByCourseId(Integer courseId) {
        String sql = "SELECT * FROM course_catalog WHERE course_id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(CourseDTO.class), courseId);
    }

    public void updateCourseCatalog(Integer courseId, String staffId, LocalDate courseDate, LocalTime courseStartTime, LocalTime courseEndTime, BigDecimal coursePrice) {
        String sql = "UPDATE course_catalog SET staff_id = ?, course_date = ?, course_start_time = ?, course_end_time = ?, course_price = ? WHERE course_id = ?";
        jdbcTemplate.update(sql, staffId, courseDate, courseStartTime, courseEndTime, coursePrice, courseId);
    }
}
