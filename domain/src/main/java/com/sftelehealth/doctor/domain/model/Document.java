package com.sftelehealth.doctor.domain.model;

/**
 * Created by Rahul on 19/06/17.
 */

public class Document {

    private String id;
    private String url;
    private String note;
    private String title;
    private String oldId;
    private Integer patientId;
    private Integer documentCategoryId;
    private String createdAt;
    private String updatedAt;
    private String previewUrl;

    public static final String PDF = "pdf";
    public static final String IMAGE = "image";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOldId() {
        return oldId;
    }

    public void setOldId(String oldId) {
        this.oldId = oldId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getDocumentCategoryId() {
        return documentCategoryId;
    }

    public void setDocumentCategoryId(Integer documentCategoryId) {
        this.documentCategoryId = documentCategoryId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getDocumentType() {
        String dataType = "jpg";
        int i = this.getPreviewUrl().lastIndexOf('.');
        if (i > 0) {
            dataType = this.getPreviewUrl().substring(i+1);
        }
        return (dataType.equalsIgnoreCase("pdf") ? PDF : IMAGE);
    }

    public String getDocumentFileExtension() {
        String dataType = "jpg";
        int i = this.getPreviewUrl().lastIndexOf('.');
        if (i > 0) {
            dataType = this.getPreviewUrl().substring(i+1);
        }
        return dataType;
    }
}
