package com.alpro.physio.dto;

public class UploadPdfDTO {

    private Integer id;
    private Integer moduleId;
    private String uploadFilename;
    private String uploadFilepath;

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getModuleId() {
        return moduleId;
    }

    public void setModuleId(Integer moduleId) {
        this.moduleId = moduleId;
    }

    public String getUploadFilename() {
        return uploadFilename;
    }

    public void setUploadFilename(String uploadFilename) {
        this.uploadFilename = uploadFilename;
    }

    public String getUploadFilepath() {
        return uploadFilepath;
    }

    public void setUploadFilepath(String uploadFilepath) {
        this.uploadFilepath = uploadFilepath;
    }
}
