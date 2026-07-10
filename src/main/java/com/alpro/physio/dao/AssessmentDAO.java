package com.alpro.physio.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.alpro.physio.dto.AssessmentDTO;

@Repository
public class AssessmentDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Inserts a new assessment under a given course.
     * One course can have multiple assessments, so no uniqueness constraint on courseId.
     */
    public AssessmentDTO insertAssessmentByCourseId(Integer courseId, String title) {
        String sql = "INSERT INTO assessment (course_id, title) VALUES (?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, courseId);
            ps.setString(2, title);
            return ps;
        }, keyHolder);

        AssessmentDTO assessment = new AssessmentDTO();
        assessment.setId(keyHolder.getKey().intValue());
        assessment.setCourseId(courseId);
        assessment.setTitle(title);

        return assessment;
    }

    /**
     * Returns all assessments belonging to a specific course.
     */
    public List<AssessmentDTO> findAssessmentsByCourseId(Integer courseId) {
        String sql = "SELECT * FROM assessment WHERE course_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(AssessmentDTO.class), courseId);
    }
}