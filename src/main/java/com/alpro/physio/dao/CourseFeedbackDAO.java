package com.alpro.physio.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.alpro.physio.dto.CourseFeedbackDTO;

@Repository
public class CourseFeedbackDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<CourseFeedbackDTO> getAllFeedback() {
        String sql = "SELECT * FROM course_feedback";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CourseFeedbackDTO.class));
    }

    public void insertFeedback(Integer enrollCourseId, Integer rate, String review) {
        String sql = "INSERT INTO course_feedback (enroll_course_id, rate, review) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, enrollCourseId, rate, review);
    }

    public boolean feedbackExistsForEnrollment(Integer enrollCourseId) {
        String sql = "SELECT COUNT(*) FROM course_feedback WHERE enroll_course_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, enrollCourseId);
        return count != null && count > 0;
    }
}
