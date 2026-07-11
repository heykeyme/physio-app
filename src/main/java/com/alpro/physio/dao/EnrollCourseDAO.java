package com.alpro.physio.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.alpro.physio.dto.CourseDTO;
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

    public List<EnrollCourseDTO> findAllCourseEnrollByUser(String userId) {
      String sql = "SELECT * FROM enroll_course WHERE user_id = ?";
      return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EnrollCourseDTO.class), userId);
    }

    public Integer updateParticipantAttendance(String userId, int courseId, Integer attendanceStatus) {
      String sql = "UPDATE enroll_course SET attendance_status = ? WHERE user_id = ? AND course_id = ?";
      return jdbcTemplate.update(sql, attendanceStatus, userId, courseId);
    }

    public List<CourseDTO> findCoursesNotEnrolledByUser(String userId) {
      String sql = "SELECT c.id, c.course_name, c.staff_id, c.course_date, "
                + "c.course_start_time, c.course_end_time, c.course_price, c.status "
                + "FROM course c "
                + "WHERE NOT EXISTS ("
                + "    SELECT 1 FROM enroll_course ec "
                + "    WHERE ec.course_id = c.id AND ec.user_id = ?"
                + ") "
                + "ORDER BY c.course_date ASC";

      return jdbcTemplate.query(sql, (rs, rowNum) -> {
          CourseDTO course = new CourseDTO();
          course.setId(rs.getInt("id"));
          course.setCourseName(rs.getString("course_name"));
          course.setStaffId(rs.getString("staff_id"));
          course.setCourseDate(rs.getObject("course_date", java.time.LocalDate.class));
          course.setCourseStartTime(rs.getObject("course_start_time", java.time.LocalTime.class));
          course.setCourseEndTime(rs.getObject("course_end_time", java.time.LocalTime.class));
          course.setCoursePrice(rs.getBigDecimal("course_price"));
          course.setStatus(rs.getInt("status"));
          return course;
      }, userId);
    }

    public void enrollParticipant(String userId, Integer courseId) {
      String sql = "INSERT INTO enroll_course (user_id, course_id) VALUES (?, ?)";
      jdbcTemplate.update(sql, userId, courseId);
    }

    public List<EnrollCourseDTO> findCompletedEnrollmentsByUser(String userId) {
      String sql = "SELECT * FROM enroll_course WHERE user_id = ? AND course_status = 1";
      return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EnrollCourseDTO.class), userId);
    }

    public int countParticipantsWithAtLeastOneCompletedCourse() {
      String sql = "SELECT COUNT(DISTINCT user_id) FROM enroll_course WHERE course_status = 1";
      Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
      return count != null ? count : 0;
    }

    public EnrollCourseDTO findEnrollmentByUserAndCourse(String userId, Integer courseId) {
      String sql = "SELECT * FROM enroll_course WHERE user_id = ? AND course_id = ?";
      return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EnrollCourseDTO.class), userId, courseId)
              .stream().findFirst().orElse(null);
    }

    public void updateCourseStatus(String userId, Integer courseId, Integer status) {
      String sql = "UPDATE enroll_course SET course_status = ? WHERE user_id = ? AND course_id = ?";
      jdbcTemplate.update(sql, status, userId, courseId);
    }
}
