package com.alpro.physio.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.alpro.physio.dto.PaymentTransactionDTO;

@Repository
public class PaymentTransactionDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insertPendingTransaction(String billCode, String userId, Integer courseId, BigDecimal amount) {
        String sql = "INSERT INTO payment_transaction (bill_code, user_id, course_id, amount, status) VALUES (?, ?, ?, ?, 0)";
        jdbcTemplate.update(sql, billCode, userId, courseId, amount);
    }

    public PaymentTransactionDTO findByBillCode(String billCode) {
        String sql = "SELECT * FROM payment_transaction WHERE bill_code = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PaymentTransactionDTO.class), billCode)
                .stream().findFirst().orElse(null);
    }

    public void updateStatus(String billCode, Integer status) {
        String sql = "UPDATE payment_transaction SET status = ? WHERE bill_code = ?";
        jdbcTemplate.update(sql, status, billCode);
    }

    public List<Map<String, Object>> findRecentSuccessfulPayments(int limit) {
    String sql = "SELECT pt.amount, pt.created_at, c.course_name, u.fullname "
               + "FROM payment_transaction pt "
               + "JOIN course c ON c.id = pt.course_id "
               + "JOIN `user` u ON u.user_id = pt.user_id "
               + "WHERE pt.status = 1 "
               + "ORDER BY pt.created_at DESC LIMIT ?";
    return jdbcTemplate.queryForList(sql, limit);
}
}