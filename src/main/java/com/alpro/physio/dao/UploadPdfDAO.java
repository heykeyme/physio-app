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

import com.alpro.physio.dto.UploadPdfDTO;

@Repository
public class UploadPdfDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Inserts a new PDF under a given module.
     * One module can have multiple PDFs, so no uniqueness constraint on moduleId.
     */
    public UploadPdfDTO insertPdfByModuleId(Integer moduleId, String uploadFilename, String uploadFilepath) {
        String sql = "INSERT INTO upload_pdf (module_id, upload_filename, upload_filepath) VALUES (?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, moduleId);
            ps.setString(2, uploadFilename);
            ps.setString(3, uploadFilepath);
            return ps;
        }, keyHolder);

        int newId = keyHolder.getKey().intValue();

        return findPdfById(newId);
    }

    /**
     * Returns all PDFs belonging to a specific module.
     */
    public List<UploadPdfDTO> findPdfsByModuleId(Integer moduleId) {
        String sql = "SELECT * FROM upload_pdf WHERE module_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UploadPdfDTO.class), moduleId);
    }

    /**
     * Returns a single PDF by its own id.
     */
    public UploadPdfDTO findPdfById(Integer id) {
        String sql = "SELECT * FROM upload_pdf WHERE id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UploadPdfDTO.class), id)
                .stream().findFirst().orElse(null);
    }

    /**
     * Deletes a single PDF by id. Returns rows affected (0 if not found).
     */
    public Integer deletePdfById(Integer id) {
        String sql = "DELETE FROM upload_pdf WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}