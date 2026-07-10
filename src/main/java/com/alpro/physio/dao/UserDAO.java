package com.alpro.physio.dao;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

    // Admin usage to register staff
    public void registerStaff(String email, String fullname, String hashedPassword, int roleId) {
        String userId = LETTERS.charAt(RANDOM.nextInt(26)) + ""
                    + LETTERS.charAt(RANDOM.nextInt(26))
                    + LETTERS.charAt(RANDOM.nextInt(26))
                    + String.format("%06d", RANDOM.nextInt(1_000_000));

        String sql = "INSERT INTO `user` (user_id, email, fullname, password, status, role_id) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, email, fullname, hashedPassword, 1, roleId);
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
            user.setStatus(rs.getInt("status"));
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
        String sql = "SELECT id, user_id, email, fullname, status, role_id FROM `user` WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(UserDTO.class), userId);
    }

    public List<UserDTO> findUserByRoleId(Integer roleId) {
        String sql = "SELECT * FROM `user` WHERE role_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserDTO.class), roleId);
    }

    public List<UserDTO> findAllUsers(int page) {
        int pageSize = 10;
        int offset = (page - 1) * pageSize; // assumes page is 1-indexed

        String sql = "SELECT id, user_id, email, fullname, password, status, role_id "
               + "FROM `user` ORDER BY id ASC LIMIT ? OFFSET ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserDTO user = new UserDTO();
            user.setId(rs.getInt("id"));
            user.setUserId(rs.getString("user_id"));
            user.setEmail(rs.getString("email"));
            user.setFullname(rs.getString("fullname"));
            user.setPassword(rs.getString("password"));
            user.setStatus(rs.getInt("status"));
            user.setRoleId(rs.getInt("role_id"));
            return user;
        }, pageSize, offset);
    }

    public List<UserDTO> findParticipantsAndTrainers(int page) {
        int pageSize = 10;
        int offset = (page - 1) * pageSize; // assumes page is 1-indexed

        String sql = "SELECT id, user_id, email, fullname, password, status, role_id "
                + "FROM `user` WHERE role_id IN (3, 4) ORDER BY id ASC LIMIT ? OFFSET ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserDTO user = new UserDTO();
            user.setId(rs.getInt("id"));
            user.setUserId(rs.getString("user_id"));
            user.setEmail(rs.getString("email"));
            user.setFullname(rs.getString("fullname"));
            user.setPassword(rs.getString("password"));
            user.setStatus(rs.getInt("status"));
            user.setRoleId(rs.getInt("role_id"));
            return user;
        }, pageSize, offset);
    }

    public int countParticipantsAndTrainers() {
        String sql = "SELECT COUNT(*) FROM `user` WHERE role_id IN (3, 4)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Returns total user count, needed by the controller to calculate total pages.
     */
    public int countAllUsers() {
        String sql = "SELECT COUNT(*) FROM `user`";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public Integer changeStatusUser(Integer id, Integer status) {
        String sql = "UPDATE `user` SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, id);
    }

    public List<UserDTO> searchUserByFullname(String fullname, int page) {
        int pageSize = 10;
        int offset = (page - 1) * pageSize; // assumes page is 1-indexed

        String sql = "SELECT id, user_id, email, fullname, password, status, role_id "
                + "FROM `user` WHERE fullname LIKE ? ORDER BY id ASC LIMIT ? OFFSET ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserDTO user = new UserDTO();
            user.setId(rs.getInt("id"));
            user.setUserId(rs.getString("user_id"));
            user.setEmail(rs.getString("email"));
            user.setFullname(rs.getString("fullname"));
            user.setPassword(rs.getString("password"));
            user.setStatus(rs.getInt("status"));
            user.setRoleId(rs.getInt("role_id"));
            return user;
        }, "%" + fullname + "%", pageSize, offset);
    }

    /**
     * Returns total matching count for the search term, needed for pagination metadata.
     */
    public int countSearchUserByFullname(String fullname) {
        String sql = "SELECT COUNT(*) FROM `user` WHERE fullname LIKE ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, "%" + fullname + "%");
        return count != null ? count : 0;
    }

    public List<UserDTO> searchParticipantsAndTrainersByFullname(String fullname, int page) {
        int pageSize = 10;
        int offset = (page - 1) * pageSize;

        String sql = "SELECT id, user_id, email, fullname, password, status, role_id "
                + "FROM `user` WHERE fullname LIKE ? AND role_id IN (3, 4) ORDER BY id ASC LIMIT ? OFFSET ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserDTO user = new UserDTO();
            user.setId(rs.getInt("id"));
            user.setUserId(rs.getString("user_id"));
            user.setEmail(rs.getString("email"));
            user.setFullname(rs.getString("fullname"));
            user.setPassword(rs.getString("password"));
            user.setStatus(rs.getInt("status"));
            user.setRoleId(rs.getInt("role_id"));
            return user;
        }, "%" + fullname + "%", pageSize, offset);
    }

    public int countSearchParticipantsAndTrainersByFullname(String fullname) {
        String sql = "SELECT COUNT(*) FROM `user` WHERE fullname LIKE ? AND role_id IN (3, 4)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, "%" + fullname + "%");
        return count != null ? count : 0;
    }

    public int countActiveParticipants() {
        String sql = "SELECT COUNT(*) FROM `user` WHERE role_id = 3 AND status = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public int countAllParticipants() {
        String sql = "SELECT COUNT(*) FROM `user` WHERE role_id = 3";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public UserDTO findUserById(Integer id) {
        String sql = "SELECT id, user_id, email, fullname, status, role_id FROM `user` WHERE id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserDTO.class), id)
                .stream().findFirst().orElse(null);
    }
}
