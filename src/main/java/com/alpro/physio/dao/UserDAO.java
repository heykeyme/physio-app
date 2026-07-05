package com.alpro.physio.dao;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.alpro.physio.dto.UserDTO;

@Repository
public class UserDAO {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    public boolean isEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM `user` WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public void registerUser(String email, String fullname, String password) {
        String userId = LETTERS.charAt(RANDOM.nextInt(26)) + ""
                       + LETTERS.charAt(RANDOM.nextInt(26))
                       + LETTERS.charAt(RANDOM.nextInt(26))
                       + String.format("%06d", RANDOM.nextInt(1_000_000));

        String sql = "INSERT INTO `user` (user_id, email, fullname, password, status, role_id) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, email, fullname, password, 1, 3);
    }

    public UserDTO findByEmail(String email) {
        String sql = "SELECT id, user_id, email, fullname, password, status, role_id FROM `user` WHERE email = ?";

        List<UserDTO> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserDTO user = new UserDTO();
            user.setId(rs.getInt("id"));
            user.setUserId(rs.getString("user_id"));
            user.setEmail(rs.getString("email"));
            user.setFullname(rs.getString("fullname"));
            user.setPassword(rs.getString("password"));
            user.setStatus(rs.getBoolean("status"));
            user.setRoleId(rs.getInt("role_id"));
            return user;
        }, email);

        return results.isEmpty() ? null : results.get(0);
    }

    public List<UserDTO> findTrainer() {
        String sql = "SELECT user_id, fullname FROM `user` WHERE role_id = 4 AND status = 1";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserDTO user = new UserDTO();
            user.setUserId(rs.getString("user_id"));
            user.setFullname(rs.getString("fullname"));
            return user;
        });
    }

    public UserDTO findByUserId(String userId) {
        String sql = "SELECT fullname FROM `user` WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(UserDTO.class), userId);
    }
}
