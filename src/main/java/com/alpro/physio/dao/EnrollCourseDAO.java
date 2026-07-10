package com.alpro.physio.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.alpro.physio.dto.EnrollCourseDTO;

@Repository
public class EnrollCourseDAO {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public EnrollCourseDTO findEnrollCourseByCourseId(int course_id) {
      String sql = "SELECT * FROM enroll_course WHERE course_id = ?";
      return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EnrollCourseDTO.class), course_id).stream().findFirst().orElse(null);
    }

    public EnrollCourseDTO findEnrollCourseById(int id) {
      String sql = "SELECT * FROM enroll_course WHERE id = ?";
      return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EnrollCourseDTO.class), id).stream().findFirst().orElse(null);
    }

    public int countEnrollmentByCourseId(int course_id) {
      String sql = "SELECT COUNT(*) FROM enroll_course WHERE course_id = ?";
      Integer count = jdbcTemplate.queryForObject(sql, Integer.class, course_id);

      return (count != null) ? count : 0;
    }

    public List<EnrollCourseDTO> findAllParticipantByCourseId(int course_id) {
      String sql = "SELECT * FROM enroll_course WHERE course_id = ?";
      return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EnrollCourseDTO.class), course_id);
    }

    public Integer updateParticipantAttendance(String userId, int courseId, Integer attendanceStatus) {
      String sql = "UPDATE enroll_course SET attendance_status = ? WHERE user_id = ? AND course_id = ?";
      return jdbcTemplate.update(sql, attendanceStatus, userId, courseId);
    }
}
