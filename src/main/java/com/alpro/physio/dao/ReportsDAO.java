package com.alpro.physio.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReportsDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ===== ENROLLMENT =====

    public Map<String, Object> getEnrollmentTotals() {
        String sql = "SELECT COUNT(*) AS total, "
                + "SUM(CASE WHEN course_status = 1 THEN 1 ELSE 0 END) AS completed, "
                + "SUM(CASE WHEN course_status = 0 THEN 1 ELSE 0 END) AS ongoing "
                + "FROM enroll_course";
        return jdbcTemplate.queryForMap(sql);
    }

    public List<Map<String, Object>> getEnrollmentByCourse() {
        String sql = "SELECT c.course_name, COUNT(ec.id) AS enrollment_count "
                + "FROM course c LEFT JOIN enroll_course ec ON ec.course_id = c.id "
                + "GROUP BY c.id, c.course_name "
                + "ORDER BY enrollment_count DESC LIMIT 5";
        return jdbcTemplate.queryForList(sql);
    }

    // ===== ATTENDANCE =====

    public Map<String, Object> getAttendanceTotals() {
        String sql = "SELECT COUNT(*) AS total, "
                + "SUM(CASE WHEN attendance_status = 1 THEN 1 ELSE 0 END) AS present, "
                + "SUM(CASE WHEN attendance_status = 0 THEN 1 ELSE 0 END) AS absent "
                + "FROM enroll_course";
        return jdbcTemplate.queryForMap(sql);
    }

    public List<Map<String, Object>> getAttendanceByCourse() {
        String sql = "SELECT c.course_name, "
                + "COUNT(ec.id) AS total, "
                + "SUM(CASE WHEN ec.attendance_status = 1 THEN 1 ELSE 0 END) AS present "
                + "FROM course c LEFT JOIN enroll_course ec ON ec.course_id = c.id "
                + "GROUP BY c.id, c.course_name "
                + "ORDER BY total DESC LIMIT 5";
        return jdbcTemplate.queryForList(sql);
    }

    // ===== REVENUE =====

    public Map<String, Object> getRevenueTotals() {
        String sql = "SELECT COALESCE(SUM(CASE WHEN status = 1 THEN amount ELSE 0 END), 0) AS total_revenue, "
                + "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS pending_count, "
                + "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS failed_count, "
                + "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS success_count "
                + "FROM payment_transaction";
        return jdbcTemplate.queryForMap(sql);
    }

    public List<Map<String, Object>> getRevenueByCourse() {
        String sql = "SELECT c.course_name, COALESCE(SUM(pt.amount), 0) AS revenue "
                + "FROM course c LEFT JOIN payment_transaction pt ON pt.course_id = c.id AND pt.status = 1 "
                + "GROUP BY c.id, c.course_name "
                + "ORDER BY revenue DESC LIMIT 5";
        return jdbcTemplate.queryForList(sql);
    }

    // ===== FEEDBACK =====

    public Map<String, Object> getFeedbackTotals() {
        String sql = "SELECT COUNT(*) AS total, COALESCE(AVG(rate), 0) AS avg_rating FROM course_feedback";
        return jdbcTemplate.queryForMap(sql);
    }

    public List<Map<String, Object>> getFeedbackByCourse() {
        String sql = "SELECT c.course_name, AVG(cf.rate) AS avg_rating, COUNT(cf.id) AS review_count "
                + "FROM course c "
                + "JOIN enroll_course ec ON ec.course_id = c.id "
                + "JOIN course_feedback cf ON cf.enroll_course_id = ec.id "
                + "GROUP BY c.id, c.course_name "
                + "ORDER BY avg_rating DESC LIMIT 5";
        return jdbcTemplate.queryForList(sql);
    }
}