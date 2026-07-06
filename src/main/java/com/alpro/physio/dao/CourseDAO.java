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

    public Integer createCourse(CourseDTO courseDTO) {
        String sql = "INSERT INTO course (course_name, staff_id, course_date, course_start_time, course_end_time, course_price, status) VALUES (?, ?, ?, ?, ?, ?, 1)";
        return jdbcTemplate.update(sql,
            courseDTO.getCourseName(),
            courseDTO.getStaffId(),
            courseDTO.getCourseDate(),
            courseDTO.getCourseStartTime(),
            courseDTO.getCourseEndTime(),
            courseDTO.getCoursePrice()
        );
    }

    public List<CourseDTO> getAllCourses() {
        String sql = "SELECT * FROM course";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CourseDTO.class));
    }

    public CourseDTO getCourseById(Integer courseId) {
        String sql = "SELECT * FROM course WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{courseId}, new BeanPropertyRowMapper<>(CourseDTO.class));
    }

    public CourseDTO updateCourse(CourseDTO courseDTO) {
        String sql = "UPDATE course SET course_name = ?, staff_id = ?, course_date = ?, course_start_time = ?, course_end_time = ?, course_price = ? WHERE id = ?";
        jdbcTemplate.update(sql,
            courseDTO.getCourseName(),
            courseDTO.getStaffId(),
            courseDTO.getCourseDate(),
            courseDTO.getCourseStartTime(),
            courseDTO.getCourseEndTime(),
            courseDTO.getCoursePrice(),
            courseDTO.getId()
        );
        return getCourseById(courseDTO.getId());
    }

    public CourseDTO changeStatusCourse(CourseDTO courseDTO) {
        String sql = "UPDATE course SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, courseDTO.getStatus(), courseDTO.getId());
        return getCourseById(courseDTO.getId());
    }

    public List<CourseDTO> getCoursesByTrainerId(String trainerId) {
        String sql = "SELECT * FROM course WHERE staff_id = ?";
        return jdbcTemplate.query(sql, new Object[]{trainerId}, new BeanPropertyRowMapper<>(CourseDTO.class));
    }
}
