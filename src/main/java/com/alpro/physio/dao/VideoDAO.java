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

import com.alpro.physio.dto.VideoDTO;

@Repository
public class VideoDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Inserts a new video under a given module.
     * One module can have multiple videos, so no uniqueness constraint on moduleId.
     */
    public VideoDTO insertVideoByModuleId(Integer moduleId, String videoFilename, String videoFilepath) {
        String sql = "INSERT INTO video (module_id, video_filename, video_filepath) VALUES (?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, moduleId);
            ps.setString(2, videoFilename);
            ps.setString(3, videoFilepath);
            return ps;
        }, keyHolder);

        int newId = keyHolder.getKey().intValue();

        return findVideoById(newId);
    }

    /**
     * Returns all videos belonging to a specific module.
     */
    public List<VideoDTO> findVideosByModuleId(Integer moduleId) {
        String sql = "SELECT * FROM video WHERE module_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(VideoDTO.class), moduleId);
    }

    /**
     * Returns a single video by its own id.
     */
    public VideoDTO findVideoById(Integer id) {
        String sql = "SELECT * FROM video WHERE id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(VideoDTO.class), id)
                .stream().findFirst().orElse(null);
    }

    /**
     * Deletes a single video by id. Returns rows affected (0 if not found).
     */
    public Integer deleteVideoById(Integer id) {
        String sql = "DELETE FROM video WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}