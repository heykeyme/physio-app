package com.alpro.physio.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ModuleDAO {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Integer findTotalModuleByCourseId(Integer courseId) {
        String sql = "SELECT COUNT(*) FROM module WHERE course_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, courseId);

        return (count != null) ? count : 0;
    }
}
