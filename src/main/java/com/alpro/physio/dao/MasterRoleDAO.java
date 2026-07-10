package com.alpro.physio.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.alpro.physio.dto.MasterRoleDTO;

@Repository
public class MasterRoleDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String findRoleNameById(Integer id) {
        String sql = "SELECT * FROM master_role WHERE id = ?";
        MasterRoleDTO role = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MasterRoleDTO.class), id)
                .stream().findFirst().orElse(null);
        return role != null ? role.getRoleName() : null; 
    }
}
