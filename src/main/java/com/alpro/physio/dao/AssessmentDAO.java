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
     * Inserts a new assessment under a given module.
     * One module can have multiple assessments, so no uniqueness constraint on moduleId.
     */
    public AssessmentDTO insertAssessmentByModuleId(Integer moduleId, String title) {
        String sql = "INSERT INTO assessment (module_id, title) VALUES (?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, moduleId);
            ps.setString(2, title);
            return ps;
        }, keyHolder);

        AssessmentDTO assessment = new AssessmentDTO();
        assessment.setId(keyHolder.getKey().intValue());
        assessment.setModuleId(moduleId);
        assessment.setTitle(title);

        return assessment;
    }

    /**
     * Returns all assessments belonging to a specific module.
     */
    public List<AssessmentDTO> findAssessmentsByModuleId(Integer moduleId) {
        String sql = "SELECT * FROM assessment WHERE module_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(AssessmentDTO.class), moduleId);
    }

    /**
     * Returns a single assessment by its own id — needed by the participant-facing
     * controller to walk assessment -> module -> course for the eligibility check.
     */
    public AssessmentDTO findAssessmentById(Integer id) {
        String sql = "SELECT * FROM assessment WHERE id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(AssessmentDTO.class), id)
                .stream().findFirst().orElse(null);
    }
}