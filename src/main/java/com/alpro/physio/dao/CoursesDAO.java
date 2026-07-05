package com.alpro.physio.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CoursesDAO {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Integer addCourse(String courseName) {
        String sql = "INSERT INTO courses (course_name, status) VALUES (?, 1)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, courseName);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }
}
