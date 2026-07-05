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
}
