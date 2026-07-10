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

import com.alpro.physio.dto.QuestionDTO;

@Repository
public class QuestionDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;  

    /**
     * Inserts a new question under a given assessment.
     * One assessment can have multiple questions, so no uniqueness constraint on assessmentId.
     */
    public QuestionDTO insertQuestionByAssessmentId(Integer assessmentId, String questionText) {
        String sql = "INSERT INTO question (assessment_id, question_text) VALUES (?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, assessmentId);
            ps.setString(2, questionText);
            return ps;
        }, keyHolder);

        QuestionDTO question = new QuestionDTO();
        question.setId(keyHolder.getKey().intValue());
        question.setAssessmentId(assessmentId);
        question.setQuestionText(questionText);

        return question;
    }

    public List<QuestionDTO> findQuestionsByAssessmentId(Integer assessmentId) {
        String sql = "SELECT * FROM question WHERE assessment_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(QuestionDTO.class), assessmentId);
    }
}