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

import com.alpro.physio.dto.QuestionOptionDTO;

@Repository
public class QuestionOptionDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public QuestionOptionDTO insertOption(Integer questionId, String label, String text, boolean isCorrect) {
        String sql = "INSERT INTO question_option (question_id, option_label, option_text, is_correct) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, questionId);
            ps.setString(2, label);
            ps.setString(3, text);
            ps.setBoolean(4, isCorrect);
            return ps;
        }, keyHolder);

        QuestionOptionDTO option = new QuestionOptionDTO();
        option.setId(keyHolder.getKey().intValue());
        option.setQuestionId(questionId);
        option.setOptionLabel(label);
        option.setOptionText(text);
        option.setIsCorrect(isCorrect);

        return option;
    }

    public List<QuestionOptionDTO> findOptionsByQuestionId(Integer questionId) {
        String sql = "SELECT * FROM question_option WHERE question_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(QuestionOptionDTO.class), questionId);
    }
}