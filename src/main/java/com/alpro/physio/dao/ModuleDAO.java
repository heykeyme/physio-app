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

import com.alpro.physio.dto.ModuleDTO;

@Repository
public class ModuleDAO {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Integer findTotalModuleByCourseId(Integer courseId) {
        String sql = "SELECT COUNT(*) FROM module WHERE course_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, courseId);

        return (count != null) ? count : 0;
    }

    public ModuleDTO insertModuleByCourseId(Integer courseId, String moduleName) {
        String sql = "INSERT INTO module (course_id, module_name) VALUES (?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, courseId);
            ps.setString(2, moduleName);
            return ps;
        }, keyHolder);

        ModuleDTO module = new ModuleDTO();
        module.setId(keyHolder.getKey().intValue());
        module.setCourseId(courseId);
        module.setModuleName(moduleName);

        return module;
    }

    public List<ModuleDTO> findModulesByCourseId(Integer courseId) {
        String sql = "SELECT * FROM module WHERE course_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ModuleDTO.class), courseId);
    }

    public Integer deleteModuleById(Integer id) {
        String sql = "DELETE FROM module WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
